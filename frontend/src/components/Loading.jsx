import React from 'react'

export default function Loading() {
  return (
    <div className="login-screen">
      <div className="login-card">
        <div className="badge">loading</div>
        <h1 className="login-title">Preparing your workspace</h1>
        <p className="muted">
          Contacting Keycloak and preparing your session. This should take a few seconds.
        </p>
      </div>
    </div>
  )
}
