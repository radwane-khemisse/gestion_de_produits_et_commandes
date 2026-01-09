import React, { useEffect, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth/AuthContext.jsx'

export default function Commands() {
  const { getValidToken, hasRole, username } = useAuth()
  const [commands, setCommands] = useState([])
  const [productMap, setProductMap] = useState({})
  const [expanded, setExpanded] = useState(new Set())
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const isAdmin = hasRole('ADMIN')

  const loadCommands = async () => {
    setLoading(true)
    setError(null)
    try {
      const token = await getValidToken()
      const [products, data] = await Promise.all([
        api.listProducts(token).catch(() => []),
        isAdmin ? api.listCommandes(token) : api.listCommandesByClient(username, token),
      ])
      const map = {}
      products.forEach((product) => {
        map[product.id] = product
      })
      setProductMap(map)
      setCommands(data)
    } catch (err) {
      setError(err.message || 'Unable to load commands')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCommands()
  }, [isAdmin, username])

  const errorMessage =
    error && error !== 'Bad Request'
      ? error
      : 'Quantite insuffisante. Veuillez ajuster la commande.'

  const toggleExpanded = (id) => {
    setExpanded((prev) => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  return (
    <div className="stack">
      {error && <div className="alert">{errorMessage}</div>}

      <div className="panel">
        <div className="tag">{isAdmin ? 'All commands' : 'My commands'}</div>
        {loading ? (
          <p className="muted">Loading commands...</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Client</th>
                <th>Status</th>
                <th>Total</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              {commands.map((cmd) => (
                <React.Fragment key={cmd.id}>
                  <tr>
                    <td>{cmd.id}</td>
                    <td>{cmd.clientId}</td>
                    <td>{cmd.status}</td>
                    <td>${Number(cmd.totalAmount || 0).toFixed(2)}</td>
                    <td>
                      <button
                        className="btn ghost"
                        type="button"
                        onClick={() => toggleExpanded(cmd.id)}
                      >
                        {expanded.has(cmd.id) ? 'Hide' : 'View'}
                      </button>
                    </td>
                  </tr>
                  {expanded.has(cmd.id) && (
                    <tr>
                      <td colSpan="5">
                        <div className="panel soft">
                          <div className="tag">Items</div>
                          <table className="table">
                            <thead>
                              <tr>
                                <th>Product</th>
                                <th>Qty</th>
                                <th>Price</th>
                                <th>Total</th>
                              </tr>
                            </thead>
                            <tbody>
                              {cmd.items?.map((item, index) => (
                                <tr key={`${cmd.id}-item-${index}`}>
                                  <td>
                                    {productMap[item.productId]
                                      ? `${productMap[item.productId].name} (#${item.productId})`
                                      : `#${item.productId}`}
                                  </td>
                                  <td>{item.quantity}</td>
                                  <td>${Number(item.price || 0).toFixed(2)}</td>
                                  <td>${Number(item.lineTotal || 0).toFixed(2)}</td>
                                </tr>
                              ))}
                              {!cmd.items?.length && (
                                <tr>
                                  <td colSpan="4" className="muted">
                                    No items found
                                  </td>
                                </tr>
                              )}
                            </tbody>
                          </table>
                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              ))}
              {commands.length === 0 && (
                <tr>
                  <td colSpan="5" className="muted">
                    No commands yet
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
