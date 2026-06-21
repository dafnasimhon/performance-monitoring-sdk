export default function SlowestDevices({ data }) {
  if (!data || data.length === 0) {
    return (
      <div className="table-section">
        <h2>Slowest Devices</h2>
        <div className="empty">No device data in this range.</div>
      </div>
    )
  }

  return (
    <div className="table-section">
      <h2>Slowest Devices</h2>
      <table>
        <thead>
          <tr>
            <th>Device Model</th>
            <th>Avg Startup</th>
            <th>P95 Startup</th>
            <th>Avg Screen</th>
            <th>P95 Screen</th>
            <th>Avg Network</th>
            <th>P95 Network</th>
            <th>Events</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <tr key={i}>
              <td className="mono">{row.deviceModel}</td>
              <td><Ms val={row.avgStartupMs} t={[500,  1000, 2000]} /></td>
              <td><Ms val={row.p95StartupMs} t={[500,  1000, 2000]} /></td>
              <td><Ms val={row.avgScreenMs}  t={[200,  500,  1000]} /></td>
              <td><Ms val={row.p95ScreenMs}  t={[200,  500,  1000]} /></td>
              <td><Ms val={row.avgNetworkMs} t={[300,  700,  1500]} /></td>
              <td><Ms val={row.p95NetworkMs} t={[300,  700,  1500]} /></td>
              <td>{row.count}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function Ms({ val, t }) {
  if (val == null) return <span style={{ color: '#9ca3af' }}>—</span>
  const [good, warn, bad] = t
  const color = val < good ? '#16a34a' : val < warn ? '#d97706' : val < bad ? '#ea580c' : '#dc2626'
  return <span style={{ color, fontWeight: 600 }}>{Math.round(val)} ms</span>
}
