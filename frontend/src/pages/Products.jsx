import React, { useEffect, useMemo, useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth/AuthContext.jsx'
import gadgetOne from '../assets/catalog/gadget-1.svg'
import gadgetTwo from '../assets/catalog/gadget-2.svg'
import gadgetThree from '../assets/catalog/gadget-3.svg'
import gadgetFour from '../assets/catalog/gadget-4.svg'
import gadgetFive from '../assets/catalog/gadget-5.svg'

const emptyForm = {
  name: '',
  description: '',
  price: '',
  quantity: '',
}

const productImages = [gadgetOne, gadgetTwo, gadgetThree, gadgetFour, gadgetFive]

export default function Products() {
  const { getValidToken, hasRole, username } = useAuth()
  const [products, setProducts] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)
  const [cart, setCart] = useState([])
  const [quantities, setQuantities] = useState({})
  const [showOrder, setShowOrder] = useState(false)
  const [showForm, setShowForm] = useState(false)

  const isAdmin = hasRole('ADMIN')

  const loadProducts = async () => {
    setLoading(true)
    setError(null)
    setSuccess(null)
    try {
      const token = await getValidToken()
      const data = await api.listProducts(token)
      setProducts(data)
    } catch (err) {
      setError(err.message || 'Unable to load products')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadProducts()
  }, [])

  const submit = async (event) => {
    event.preventDefault()
    setError(null)
    setSuccess(null)
    try {
      const token = await getValidToken()
      const payload = {
        name: form.name.trim(),
        description: form.description.trim(),
        price: Number(form.price),
        quantity: Number(form.quantity),
      }
      if (!payload.name || Number.isNaN(payload.price) || Number.isNaN(payload.quantity)) {
        setError('Please fill all required fields')
        return
      }
      let savedProduct
      if (editingId) {
        savedProduct = await api.updateProduct(editingId, payload, token)
      } else {
        savedProduct = await api.createProduct(payload, token)
      }
      setForm(emptyForm)
      setEditingId(null)
      setShowForm(false)
      setSuccess(editingId ? 'Product updated successfully' : 'Product created successfully')
      loadProducts()
    } catch (err) {
      setError(err.message || 'Unable to save product')
    }
  }

  const startEdit = (product) => {
    setEditingId(product.id)
    setForm({
      name: product.name || '',
      description: product.description || '',
      price: product.price ?? '',
      quantity: product.quantity ?? '',
    })
    setShowForm(true)
    setSuccess(null)
    setError(null)
  }

  const cancelEdit = () => {
    setEditingId(null)
    setForm(emptyForm)
    setShowForm(false)
    setSuccess(null)
    setError(null)
  }

  const removeProduct = async (productId) => {
    setError(null)
    try {
      const token = await getValidToken()
      await api.deleteProduct(productId, token)
      loadProducts()
    } catch (err) {
      setError(err.message || 'Unable to delete product')
    }
  }

  const addToCart = (product) => {
    const quantity = Number(quantities[product.id] || 1)
    if (quantity <= 0) {
      setError('Quantity must be at least 1')
      return
    }
    if (quantity > product.quantity) {
      setError(`Only ${product.quantity} in stock for ${product.name}`)
      return
    }
    setCart((prev) => {
      const existing = prev.find((item) => item.productId === product.id)
      if (existing) {
        return prev.map((item) =>
          item.productId === product.id
            ? { ...item, quantity }
            : item,
        )
      }
      return [
        ...prev,
        {
          productId: product.id,
          name: product.name,
          price: product.price,
          quantity,
        },
      ]
    })
  }

  const updateCartQty = (productId, quantity) => {
    setCart((prev) =>
      prev.map((item) =>
        item.productId === productId
          ? { ...item, quantity: Number(quantity) }
          : item,
      ),
    )
  }

  const removeFromCart = (productId) => {
    setCart((prev) => prev.filter((item) => item.productId !== productId))
  }

  const placeOrder = async () => {
    setError(null)
    if (cart.length === 0) {
      setError('Add products to your cart')
      return
    }
    try {
      const token = await getValidToken()
      await api.createCommande(
        {
          clientId: username,
          items: cart.map((item) => ({
            productId: item.productId,
            quantity: Number(item.quantity),
          })),
        },
        token,
      )
      setCart([])
      setQuantities({})
    } catch (err) {
      setError(err.message || 'Unable to create command')
    }
  }

  const cartTotal = cart.reduce(
    (sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0),
    0,
  )

  if (isAdmin) {
    return (
      <div className="stack">
        {error && <div className="alert">{error}</div>}
        {success && <div className="alert">{success}</div>}

        <div className="stack">
          {showForm && (
            <div className="panel soft">
              <div className="tag">{editingId ? 'Edit product' : 'New product'}</div>
              {isAdmin ? (
                <form className="form-grid" onSubmit={submit}>
                  <div className="form-row">
                    <label>Name</label>
                    <input
                      className="input"
                      value={form.name}
                      onChange={(event) => setForm({ ...form, name: event.target.value })}
                      placeholder="Product name"
                    />
                  </div>
                  <div className="form-row">
                    <label>Description</label>
                    <textarea
                      className="textarea"
                      value={form.description}
                      onChange={(event) =>
                        setForm({ ...form, description: event.target.value })
                      }
                      placeholder="Short description"
                    />
                  </div>
                  <div className="form-row">
                    <label>Price</label>
                    <input
                      className="input"
                      type="number"
                      step="0.01"
                      value={form.price}
                      onChange={(event) => setForm({ ...form, price: event.target.value })}
                      placeholder="0.00"
                    />
                  </div>
                  <div className="form-row">
                    <label>Quantity</label>
                    <input
                      className="input"
                      type="number"
                      value={form.quantity}
                      onChange={(event) =>
                        setForm({ ...form, quantity: event.target.value })
                      }
                      placeholder="0"
                    />
                  </div>
                  <div className="actions">
                    <button className="btn" type="submit">
                      {editingId ? 'Update' : 'Create'}
                    </button>
                    {editingId && (
                      <button
                        className="btn secondary"
                        type="button"
                        onClick={cancelEdit}
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </form>
              ) : (
                <p className="muted">
                  You have a read-only view. Login as ADMIN to create or edit products.
                </p>
              )}
            </div>
          )}

          <div className="panel">
            <div className="panel-header">
              <div className="tag">Catalog</div>
              <button className="btn ghost" type="button" onClick={() => setShowForm(!showForm)}>
                {showForm ? 'Hide form' : 'Add product'}
              </button>
            </div>
            {loading ? (
              <p className="muted">Loading products...</p>
            ) : (
              <div className="product-grid">
                {products.map((product, index) => (
                  <div className="product-card" key={product.id}>
                    <div className="product-image">
                    <img
                      src={`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888'}/catalog/${product.id}.jpg`}
                      onError={(event) => {
                        event.currentTarget.onerror = null
                        event.currentTarget.src = productImages[index % productImages.length]
                      }}
                      alt={product.name}
                    />
                    </div>
                    <div className="product-meta">
                      <h3>{product.name}</h3>
                      <p className="muted">{product.description}</p>
                      <div className="price-row">
                        <span className="price">
                          ${Number(product.price || 0).toFixed(2)}
                        </span>
                        <span className="chip">{product.quantity} in stock</span>
                      </div>
                      <div className="actions">
                        <button
                          className="btn ghost"
                          type="button"
                          onClick={() => startEdit(product)}
                        >
                          Edit
                        </button>
                        <button
                          className="btn danger"
                          type="button"
                          onClick={() => removeProduct(product.id)}
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
                {products.length === 0 && <p className="muted">No products available</p>}
              </div>
            )}
          </div>

        </div>
      </div>
    )
  }

  return (
    <div className="stack">
      {error && <div className="alert">{error}</div>}
      <div className="panel">
        <div className="panel-header">
          <div className="tag">Catalog</div>
          <button className="btn ghost" type="button" onClick={() => setShowOrder(!showOrder)}>
            {showOrder ? 'Hide order' : 'Your order'} ({cart.length})
          </button>
        </div>
        {loading ? (
          <p className="muted">Loading products...</p>
        ) : (
          <div className="product-grid">
              {products.map((product, index) => (
                <div className="product-card" key={product.id}>
                  <div className="product-image">
                    <img
                      src={`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888'}/catalog/${product.id}.jpg`}
                      onError={(event) => {
                        event.currentTarget.onerror = null
                        event.currentTarget.src = productImages[index % productImages.length]
                      }}
                      alt={product.name}
                    />
                  </div>
                <div className="product-meta">
                  <h3>{product.name}</h3>
                  <p className="muted">{product.description}</p>
                  <div className="price-row">
                    <span className="price">${Number(product.price || 0).toFixed(2)}</span>
                    <span className="chip">{product.quantity} in stock</span>
                  </div>
                  <div className="actions">
                    <input
                      className="input qty-input"
                      type="number"
                      min="1"
                      max={product.quantity}
                      value={quantities[product.id] ?? 1}
                      onChange={(event) =>
                        setQuantities({
                          ...quantities,
                          [product.id]: event.target.value,
                        })
                      }
                    />
                    <button
                      className="btn"
                      type="button"
                      onClick={() => addToCart(product)}
                    >
                      Add
                    </button>
                  </div>
                </div>
              </div>
            ))}
            {products.length === 0 && <p className="muted">No products available</p>}
          </div>
        )}
      </div>

      {showOrder && (
        <div className="panel soft">
          <div className="tag">Your order</div>
          <div className="stack">
            {cart.length === 0 && <p className="muted">Your cart is empty.</p>}
            {cart.map((item) => (
              <div className="cart-item" key={item.productId}>
                <div>
                  <strong>{item.name}</strong>
                  <div className="muted">${Number(item.price || 0).toFixed(2)}</div>
                </div>
                <div className="actions">
                  <input
                    className="input qty-input"
                    type="number"
                    min="1"
                    value={item.quantity}
                    onChange={(event) => updateCartQty(item.productId, event.target.value)}
                  />
                  <button
                    className="btn danger"
                    type="button"
                    onClick={() => removeFromCart(item.productId)}
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>
          <div className="divider" />
          <div className="actions">
            <span className="chip">Total: ${cartTotal.toFixed(2)}</span>
            <button className="btn" type="button" onClick={placeOrder}>
              Place order
            </button>
          </div>
          <div className="helper">Orders will be created through the API gateway.</div>
        </div>
      )}
    </div>
  )
}
