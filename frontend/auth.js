// Shared authentication helpers for the store and the admin dashboard
const AUTH_KEY = "crave_auth";

const Auth = {
  get() {
    try { return JSON.parse(localStorage.getItem(AUTH_KEY)) || null; }
    catch { return null; }
  },
  save(data) { localStorage.setItem(AUTH_KEY, JSON.stringify(data)); },
  clear() { localStorage.removeItem(AUTH_KEY); },
  token() { return Auth.get()?.token || null; },
  isLoggedIn() { return !!Auth.token(); },
  isAdmin() { return Auth.get()?.role === "ADMIN"; },
  headers(extra = {}) {
    const t = Auth.token();
    return t ? { ...extra, Authorization: `Bearer ${t}` } : { ...extra };
  }
};

// Turn a failed response into a readable message
async function readError(response) {
  const text = await response.text();
  try {
    const json = JSON.parse(text);
    return json.message || json.error || `HTTP ${response.status}`;
  } catch {
    return text || `HTTP ${response.status}`;
  }
}

async function loginRequest(baseUrl, email, password) {
  const res = await fetch(`${baseUrl}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password })
  });
  if (!res.ok) throw new Error(await readError(res));
  const data = await res.json();
  Auth.save(data);
  return data;
}

async function registerRequest(baseUrl, name, email, password) {
  const res = await fetch(`${baseUrl}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, email, password })
  });
  if (!res.ok) throw new Error(await readError(res));
  return res.json();
}
