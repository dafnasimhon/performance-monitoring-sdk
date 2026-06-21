export default function TracesTable({ data }) {
  if (!data || data.length === 0) {
    return <div className="empty">No custom trace data in this range.</div>
  }

  return (
    <div className="table-section">
      <h2>Custom Traces</h2>
      <table>
        <thead>
          <tr>
            <th>Trace Name</th>
            <th>Avg (ms)</th>
            <th>P95 (ms)</th>
            <th>Count</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row) => (
            <tr key={row.traceName}>
              <td>{row.traceName}</td>
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
