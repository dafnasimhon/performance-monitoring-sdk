export default function NetworkTable({ data }) {
  if (!data || data.length === 0) {
    return <div className="empty">No network request data in this range.</div>
  }

  return (
    <div className="table-section">
      <h2>Slow Endpoints</h2>
      <table>
        <thead>
          <tr>
            <th>Method</th>
            <th>Endpoint</th>
            <th>Avg (ms)</th>
            <th>P95 (ms)</th>
            <th>Error %</th>
            <th>Count</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <tr key={i}>
              <td><span className="method-badge">{row.method}</span></td>
              <td>{row.endpoint}</td>
              <td>{Math.round(row.avgMs)}</td>
              <td>{Math.round(row.p95Ms)}</td>
              <td>{(row.errorRate * 100).toFixed(1)}%</td>
              <td>{row.count}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
