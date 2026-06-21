import axios from 'axios'

const api = axios.create({
  baseURL: 'http://127.0.0.1:8000/api/v1',
  headers: { 'X-API-Key': 'dev-api-key' },
})

export const getSummary = (params) => api.get('/metrics/summary', { params })
export const getStartup = (params) => api.get('/metrics/startup', { params })
export const getScreens = (params) => api.get('/metrics/screens', { params })
export const getNetwork = (params) => api.get('/metrics/network', { params })
export const getTraces  = (params) => api.get('/metrics/traces',  { params })
export const getNetworkErrors = (params) => api.get('/metrics/network/errors', { params })
export const getHealthScore     = (params) => api.get('/metrics/health',         { params })
export const getSlowestDevices  = (params) => api.get('/metrics/devices',        { params })
export const getVersionStats    = (params) => api.get('/metrics/versions',       { params })
export const getNetworkByType    = (params) => api.get('/metrics/network-by-type',  { params })
export const getEventsOverTime   = (params) => api.get('/metrics/events-over-time', { params })
