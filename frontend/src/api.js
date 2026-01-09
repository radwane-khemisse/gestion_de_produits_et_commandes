const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888'

async function request(path, options = {}, token) {
  const headers = new Headers(options.headers || {})
  if (!headers.has('Content-Type') && options.body && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })
  if (response.status === 204) {
    return null
  }
  let payload = null
  const text = await response.text()
  if (text) {
    try {
      payload = JSON.parse(text)
    } catch {
      payload = { message: text }
    }
  }
  if (!response.ok) {
    const message =
      payload?.message ||
      payload?.detail ||
      payload?.title ||
      payload?.reason ||
      payload?.error ||
      payload?.error_description ||
      response.statusText ||
      'Request failed'
    const error = new Error(message)
    error.status = response.status
    throw error
  }
  return payload
}

export const api = {
  listProducts: (token) => request('/api/produits', { method: 'GET' }, token),
  getProduct: (id, token) => request(`/api/produits/${id}`, { method: 'GET' }, token),
  createProduct: (data, token) =>
    request('/api/produits', { method: 'POST', body: JSON.stringify(data) }, token),
  updateProduct: (id, data, token) =>
    request(`/api/produits/${id}`, { method: 'PUT', body: JSON.stringify(data) }, token),
  deleteProduct: (id, token) =>
    request(`/api/produits/${id}`, { method: 'DELETE' }, token),
  uploadProductImage: async (id, file, token) => {
    const formData = new FormData()
    formData.append('file', file)
    return request(
      `/api/produits/${id}/image`,
      {
        method: 'POST',
        body: formData,
      },
      token,
    )
  },
  listCommandes: (token) => request('/api/commandes', { method: 'GET' }, token),
  listCommandesByClient: (clientId, token) =>
    request(`/api/commandes/client/${clientId}`, { method: 'GET' }, token),
  createCommande: (data, token) =>
    request('/api/commandes', { method: 'POST', body: JSON.stringify(data) }, token),
}
