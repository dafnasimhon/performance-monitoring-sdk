import { useState, useEffect, useCallback } from 'react'
import { getSummary, getStartup, getScreens, getNetwork, getTraces, getNetworkErrors, getHealthScore, getSlowestDevices, getVersionStats, getNetworkByType } from './api/metrics'
import Filters from './components/Filters'
import KpiCard from './components/KpiCard'
import StartupChart from './components/StartupChart'
import ScreensTable from './components/ScreensTable'
import NetworkTable from './components/NetworkTable'
import TracesTable from './components/TracesTable'
import ErrorLog from './components/ErrorLog'
import HealthScore from './components/HealthScore'
import SlowestDevices from './components/SlowestDevices'
import VersionStats from './components/VersionStats'
import NetworkComparison from './components/NetworkComparison'
import './App.css'

const DEFAULT_FILTERS = { hours: 24 * 7, appVersion: '', networkType: '', deviceModel: '' }

function buildParams(filters) {
  const now = Date.now()
  const from_ts = now - filters.hours * 60 * 60 * 1000
  const params = { from_ts, to_ts: now }
  if (filters.appVersion)  params.app_version  = filters.appVersion
  if (filters.networkType) params.network_type = filters.networkType
  if (filters.deviceModel) params.device_model = filters.deviceModel
  return params
}

export default function App() {
  const [filters, setFilters]   = useState(DEFAULT_FILTERS)
  const [summary, setSummary]   = useState(null)
  const [startup, setStartup]   = useState([])
  const [screens, setScreens]   = useState([])
  const [network, setNetwork]   = useState([])
  const [traces, setTraces]     = useState([])
  const [errors, setErrors]     = useState([])
  const [health, setHealth]         = useState(null)
  const [devices, setDevices]       = useState([])
  const [versions, setVersions]     = useState([])
  const [netByType, setNetByType]   = useState([])
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState(null)

  const fetchAll = useCallback(async () => {
    setLoading(true)
    setError(null)
    const p = buildParams(filters)
    try {
      const [s, st, sc, n, tr, er, hs, dev, ver, nbt] = await Promise.all([
        getSummary(p),
        getStartup(p),
        getScreens(p),
        getNetwork(p),
        getTraces(p),
        getNetworkErrors(p),
        getHealthScore(p),
        getSlowestDevices(p),
        getVersionStats(p),
        getNetworkByType(p),
      ])
      setSummary(s.data)
      setStartup(st.data)
      setScreens(sc.data)
      setNetwork(n.data)
      setTraces(tr.data)
      setErrors(er.data)
      setHealth(hs.data)
      setDevices(dev.data)
      setVersions(ver.data)
      setNetByType(nbt.data)
    } catch (err) {
      setError(err.message || 'Failed to load data')
    } finally {
      setLoading(false)
    }
  }, [filters])

  useEffect(() => { fetchAll() }, [fetchAll])

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>PerfSDK Dashboard</h1>
        <Filters filters={filters} onChange={setFilters} />
      </header>

      {error && <div className="error-banner">Error: {error}</div>}
      {loading && <div className="loading-bar" />}

      <HealthScore data={health} />

      <div className="kpi-row">
        <KpiCard label="Avg App Startup"  value={summary?.avgStartupMs} />
        <KpiCard label="Avg Screen Load"  value={summary?.avgScreenLoadMs} />
        <KpiCard label="Avg Network Req"  value={summary?.avgNetworkMs} />
        <KpiCard label="Total Events"     value={summary?.totalEvents} unit="" />
      </div>

      <StartupChart data={startup} />
      <SlowestDevices   data={devices} />
      <VersionStats     data={versions} />
      <NetworkComparison data={netByType} />
      <ScreensTable data={screens} />
      <NetworkTable data={network} />
      <TracesTable  data={traces} />
      <ErrorLog     data={errors} />
    </div>
  )
}
