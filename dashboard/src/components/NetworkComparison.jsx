const TYPE_ICON = { WIFI: '📶', CELLULAR: '📡', UNKNOWN: '❓', NO_INTERNET: '🚫' }

export default function NetworkComparison({ data }) {
  if (!data || data.length === 0) {
    return (
      <div className="table-section">
        <h2>WIFI vs CELLULAR Performance</h2>
        <div className="empty">No network data in this range.</div>
      </div>
    )
  }

  return (
    <div className="table-section">
      <h2>WIFI vs CELLULAR Performance</h2>
      <div className="net-compare-grid">
        {data.map((row) => (
          <div key={row.networkType} className="net-compare-card">
            <div className="net-type-header">
              <span className="net-icon">{TYPE_ICON[row.networkType] ?? '🌐'}</span>
              <span className="net-type-label">{row.networkType}</span>
            </div>
            <div className="net-stats">
              <Stat label="Avg Latency"  value={`${Math.round(row.avgMs)} ms`}  highlight={row.avgMs > 700} />
              <Stat label="P95 Latency"  value={`${Math.round(row.p95Ms)} ms`}  highlight={row.p95Ms > 1400} />
              <Stat label="Error Rate"   value={`${row.errorRate}%`}             highlight={row.errorRate > 1} />
              <Stat label="Requests"     value={row.count} />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

function Stat({ label, value, highlight }) {
  return (
    <div className="net-stat-row">
      <span className="net-stat-label">{label}</span>
      <span className="net-stat-value" style={{ color: highlight ? '#dc2626' : '#374151' }}>
        {value}
      </span>
    </div>
  )
}
