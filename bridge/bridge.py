# JARVIS bridge: app <-> Termux agent. Sirf 127.0.0.1 (isi phone) par sunta hai.
import os, sys, json, secrets, threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

AGENT_DIR = "/root/MyAgent"
AGENT = AGENT_DIR + "/agent.py"
TOKEN_FILE = AGENT_DIR + "/bridge_token.txt"
PORT = 8765

os.chdir(AGENT_DIR)
src = open(AGENT, encoding="utf-8").read()
cut = src.rfind("\nload_memory()\n")
if cut == -1:
    sys.exit("agent.py ka dhancha pehchana nahi gaya (load_memory() nahi mila)")

ns = {"__name__": "agent_core"}
exec(compile(src[:cut], AGENT, "exec"), ns)   # agent.py ko badle bina, uska loop chhodkar
ns["load_memory"]()

if os.path.exists(TOKEN_FILE):
    TOKEN = open(TOKEN_FILE).read().strip()
else:
    TOKEN = secrets.token_hex(8)
    with open(TOKEN_FILE, "w") as f:
        f.write(TOKEN)
    os.chmod(TOKEN_FILE, 0o600)

lock = threading.Lock()


class H(BaseHTTPRequestHandler):
    def _send(self, code, obj):
        b = json.dumps(obj, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(b)))
        self.end_headers()
        self.wfile.write(b)

    def _ok(self):
        got = self.headers.get("X-Token", "")
        return secrets.compare_digest(got.encode("utf-8"), TOKEN.encode("utf-8"))

    def do_GET(self):
        if self.path == "/ping" and self._ok():
            return self._send(200, {"ok": True})
        self._send(401 if not self._ok() else 404, {"error": "no"})

    def do_POST(self):
        if not self._ok():
            return self._send(401, {"error": "bad token"})
        if self.path != "/ask":
            return self._send(404, {"error": "not found"})
        try:
            n = int(self.headers.get("Content-Length", "0") or 0)
            if n > 10000:
                return self._send(413, {"error": "too big"})
            text = json.loads(self.rfile.read(n).decode("utf-8"))["text"]
        except Exception:
            return self._send(400, {"error": "bad request"})
        with lock:
            try:
                reply = ns["process_command"](text)
            except Exception as e:
                reply = "Error: " + str(e)
        self._send(200, {"reply": str(reply)})

    def log_message(self, *a):
        pass


print("JARVIS bridge chalu hai. Port", PORT)
print("Agent key (app ki Settings mein dalni hai):", TOKEN)
ThreadingHTTPServer(("127.0.0.1", PORT), H).serve_forever()
