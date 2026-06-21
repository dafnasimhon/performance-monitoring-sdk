import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts'

function VersionChart({ data, dataKey, color, title }) {
  const hasData = data.some((d) => d[dataKey] != null)
  if (!hasData) return null

  return (
    <div className="version-chart">
      <p className="version-chart-title">{title}</p>
      <ResponsiveContainer width="100%" height={200}>
        <BarChart data={data} margin={{ top: 4, right: 16, left: 0, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="version" tick={{ fontSize: 12 }} />
          <YAxis unit=" ms" tick={{ fontSize: 11 }} width={60} />
          <Tooltip formatter={(v) => v != null ? [`${Math.round(v)} ms`, title] : ['—', title]} />
          <Bar dataKey={dataKey} fill={color} radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}

export default function VersionStats({ data }) {
  if (!data || data.length === 0) {
    return (
      <div className="table-section">
        <h2>Performance by App Version</h2>
        <div className="empty">No version data in this range.</div>
      </div>
    )
  }

  const chartData = [...data].reverse().map((r) => ({
    version:   r.appVersion,
    startup:   r.avgStartupMs,
    screen:    r.avgScreenMs,
    network:   r.avgNetworkMs,
  }))

  return (
    <div className="table-section">
      <h2>Performance by App Version</h2>
      <p className="section-subtitle">Avg duration per version — taller bar = regression</p>

      <div className="version-charts-grid">
        <VersionChart data={chartData} dataKey="startup" color="#6366f1" title="App Startup (ms)" />
        <VersionChart data={chartData} dataKey="screen"  color="#8b5cf6" title="Screen Load (ms)" />
        <VersionChart data={chartData} dataKey="network" color="#a78bfa" title="Network Latency (ms)" />
      </div>

      <table>
        <thead>
          <tr>
            <th>Version</th>
            <th>Avg Startup</th>
            <th>P95 Startup</th>
            <th>Avg Screen</th>
            <th>P95 Screen</th>
            <th>Avg Network</th>
            <th>P95 Network</th>
            <th>Error Rate</th>
            <th>Events</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <tr key={i}>
              <td><span className="version-badge">{row.appVersion}</span></td>
              <td>{fmt(row.avgStartupMs)}</td>
              <td>{fmt(row.p95StartupMs)}</td>
              <td>{fmt(row.avgScreenMs)}</td>
              <td>{fmt(row.p95ScreenMs)}</td>
              <td>{fmt(row.avgNetworkMs)}</td>
              <td>{fmt(row.p95NetworkMs)}</td>
              <td>
                <span style={{ color: row.errorRate > 5 ? '#dc2626' : row.errorRate > 1 ? '#d97706' : '#16a34a', fontWeight: 600 }}>
                  {row.errorRate}%
                </span>
              </td>
              <td>{row.count}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

const fmt = (v) => v != null ? `${Math.round(v)} ms` : '—'
