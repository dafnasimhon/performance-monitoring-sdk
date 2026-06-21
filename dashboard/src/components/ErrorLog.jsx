export default function ErrorLog({ data }) {
  if (!data || data.length === 0) {
    return (
      <div className="table-section">
        <h2>Network Error Log</h2>
        <div className="empty">No network errors in this range.</div>
      </div>
    )
  }

  return (
    <div className="table-section">
      <h2>
        Network Error Log{' '}
        <span className="error-count">{data.length} error{data.length !== 1 ? 's' : ''}</span>
      </h2>
      <table>
        <thead>
          <tr>
            <th>Time</th>
            <th>Method</th>
            <th>Endpoint</th>
            <th>Status</th>
            <th>Duration</th>
            <th>Device</th>
            <th>Network</th>
            <th>Version</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <tr key={i}>
              <td className="mono">{new Date(row.timestamp).toLocaleTimeString()}</td>
              <td><span className="method-badge">{row.method}</span></td>
              <td className="mono">{row.endpoint}</td>
              <td><span className="status-error">{row.statusCode}</span></td>
              <td>{row.durationMs} ms</td>
              <td>{row.deviceModel}</td>
              <td>{row.networkType}</td>
              <td>{row.appVersion || '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
