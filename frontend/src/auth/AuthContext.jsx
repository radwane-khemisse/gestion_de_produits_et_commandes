import React, { createContext, useContext, useEffect, useMemo, useRef, useState } from 'react'
import Keycloak from 'keycloak-js'

const AuthContext = createContext(null)

const buildKeycloak = () =>
  new Keycloak({
    url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080',
    realm: import.meta.env.VITE_KEYCLOAK_REALM || 'gestion-produits_commandes',
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'frontend-app',
  })

export function AuthProvider({ children }) {
  const keycloakRef = useRef(buildKeycloak())
  const [state, setState] = useState({
    initialized: false,
    authenticated: false,
    profile: null,
    roles: [],
  })

  useEffect(() => {
    const init = async () => {
      const keycloak = keycloakRef.current
      try {
        const authenticated = await keycloak.init({
          onLoad: 'check-sso',
          pkceMethod: 'S256',
          checkLoginIframe: false,
        })
        let profile = null
        if (authenticated) {
          try {
            profile = await keycloak.loadUserProfile()
          } catch {
            profile = null
          }
        }
        const roles = keycloak.tokenParsed?.realm_access?.roles || []
        setState({
          initialized: true,
          authenticated,
          profile,
          roles,
        })
      } catch {
        setState((prev) => ({ ...prev, initialized: true }))
      }
    }
    init()
  }, [])

  const login = () => keycloakRef.current.login()
  const logout = () => keycloakRef.current.logout()

  const getValidToken = async () => {
    const keycloak = keycloakRef.current
    if (!keycloak.authenticated) {
      return null
    }
    try {
      await keycloak.updateToken(30)
      return keycloak.token
    } catch {
      return keycloak.token
    }
  }

  const value = useMemo(
    () => ({
      initialized: state.initialized,
      authenticated: state.authenticated,
      profile: state.profile,
      roles: state.roles,
      login,
      logout,
      getValidToken,
      username:
        state.profile?.username ||
        keycloakRef.current.tokenParsed?.preferred_username ||
        keycloakRef.current.tokenParsed?.sub,
      hasRole: (role) => state.roles.includes(role),
    }),
    [state],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
