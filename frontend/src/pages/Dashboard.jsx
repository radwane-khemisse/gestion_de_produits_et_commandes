import React, { useEffect, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth/AuthContext.jsx'

export default function Dashboard() {
  const { getValidToken, hasRole, username } = useAuth()
  const isAdmin = hasRole('ADMIN')
  const [stats, setStats] = useState({
    products: 0,
    commands: 0,
    totalValue: 0,
    lowStock: 0,
  })
  const [recentProducts, setRecentProducts] = useState([])
  const [recentCommands, setRecentCommands] = useState([])
  const [error, setError] = useState(null)

  useEffect(() => {
    const load = async () => {
      setError(null)
      try {
        const token = await getValidToken()
        const products = await api.listProducts(token)
        const commands = isAdmin
          ? await api.listCommandes(token)
          : await api.listCommandesByClient(username, token)

        const totalValue = commands.reduce(
          (sum, cmd) => sum + Number(cmd.totalAmount || 0),
          0,
        )
        const lowStock = products.filter((p) => Number(p.quantity) < 5).length

        setStats({
          products: products.length,
          commands: commands.length,
          totalValue,
          lowStock,
        })
        setRecentProducts(products.slice(0, 5))
        setRecentCommands(commands.slice(0, 5))
      } catch (err) {
        setError(err.message || 'Unable to load dashboard stats')
      }
    }
    load()
  }, [getValidToken, isAdmin, username])

  return (
    <div className="stack">
      {error && <div className="alert">{error}</div>}

      <div className="grid-4">
        <div className="stat-card">
          <div className="tag">Products</div>
          <div className="stat-value">{stats.products}</div>
          <div className="muted">Catalog size</div>
        </div>
        <div className="stat-card">
          <div className="tag">{isAdmin ? 'Commands' : 'My commands'}</div>
          <div className="stat-value">{stats.commands}</div>
          <div className="muted">{isAdmin ? 'Orders tracked' : 'Orders placed'}</div>
        </div>
        <div className="stat-card">
          <div className="tag">Stock alert</div>
          <div className="stat-value">{stats.lowStock}</div>
          <div className="muted">Below 5 units</div>
        </div>
        <div className="stat-card">
          <div className="tag">{isAdmin ? 'Revenue' : 'Total spending'}</div>
          <div className="stat-value">${stats.totalValue.toFixed(2)}</div>
          <div className="muted">{isAdmin ? 'Sum of orders' : 'Customer spend'}</div>
        </div>
      </div>

      <div className="grid-2">
        <div className="panel">
          <div className="tag">Recent products</div>
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Qty</th>
              </tr>
            </thead>
            <tbody>
              {recentProducts.map((product) => (
                <tr key={product.id}>
                  <td>{product.id}</td>
                  <td>{product.name}</td>
                  <td>{product.quantity}</td>
                </tr>
              ))}
              {recentProducts.length === 0 && (
                <tr>
                  <td colSpan="3" className="muted">
                    No products yet
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="panel soft">
          <div className="tag">{isAdmin ? 'Recent commands' : 'My recent commands'}</div>
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Client</th>
                <th>Status</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              {recentCommands.map((cmd) => (
                <tr key={cmd.id}>
                  <td>{cmd.id}</td>
                  <td>{cmd.clientId}</td>
                  <td>{cmd.status}</td>
                  <td>${Number(cmd.totalAmount || 0).toFixed(2)}</td>
                </tr>
              ))}
              {recentCommands.length === 0 && (
                <tr>
                  <td colSpan="4" className="muted">
                    No commands yet
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
