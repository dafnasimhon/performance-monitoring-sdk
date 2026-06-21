export default function ScreensTable({ data }) {
  if (!data || data.length === 0) {
    return <div className="empty">No screen load data in this range.</div>
  }

  return (
    <div className="table-section">
      <h2>Slow Screens</h2>
      <table>
        <thead>
          <tr>
            <th>Screen</th>
            <th>Avg (ms)</th>
            <th>P95 (ms)</th>
            <th>Count</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row) => (
            <tr key={row.screenName}>
              <td>{row.screenName}</td>
              <td>{Math.round(row.avgMs)}</td>
              <td>{Math.round(row.p95Ms)}</td>
              <td>{row.count}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
