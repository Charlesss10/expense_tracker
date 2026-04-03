import { useEffect, useState } from 'react';
import Header from '../components/Header.jsx';
import API_BASE_URL from '../config.js';

function ExpenseSummaryPage({ accountId, onLogout }) {
    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [currency, setCurrency] = useState('');

    useEffect(() => {
        async function fetchSummary() {
            setLoading(true);
            setError('');
            try {
                const token = localStorage.getItem('token');
                const res = await fetch(
                    `${API_BASE_URL}/api/expenses/summary?accountId=${accountId}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                const text = await res.text();
                if (!res.ok) {
                    try {
                        const errorData = JSON.parse(text);
                        setError(errorData.message || text || 'Failed to fetch expense summary.');
                    } catch {
                        setError(text || 'Failed to fetch expense summary.');
                    }
                    setLoading(false);
                    return;
                }
                const data = text ? JSON.parse(text) : {};
                setSummary(data);
                setCurrency(data.currency || '');
            } catch (err) {
                setError(err.message || 'Failed to fetch expense summary.');
            }
            setLoading(false);
        }
        fetchSummary();
    }, [accountId]);

    function capitalizeCategory(cat) {
        if (!cat) return 'N/A';
        return cat
            .toLowerCase()
            .replace(/(^|\s)\S/g, l => l.toUpperCase());
    }

    return (
        <>
            <Header onLogout={onLogout} />
            <div className="page-container">
                <h2 className="mb-4 text-center">Expense Summary</h2>
                <hr className="mb-4" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                {loading && <div className="text-center">Loading...</div>}
                {error && <div className="alert alert-danger text-center" style={{ borderRadius: 0 }}>{error}</div>}
                {!loading && !error && summary && (!summary.expensesByCategory || Object.keys(summary.expensesByCategory).length === 0) && (
                    <div className="alert alert-info text-center" style={{ borderRadius: 0 }}>
                        No expenses recorded yet. Add transactions to see the summary.
                    </div>
                )}
                {!loading && !error && summary && summary.expensesByCategory && Object.keys(summary.expensesByCategory).length > 0 && (
                    <div className="table-responsive">
                        <table className="table table-bordered table-striped">
                            <thead>
                                <tr>
                                    <th>Category</th>
                                    <th>Total Amount{currency ? ` (${currency})` : ''}</th>
                                    <th>Percentage (%)</th>
                                </tr>
                            </thead>
                            <tbody>
                                {Object.entries(summary.expensesByCategory).map(([cat, amt], idx) => (
                                    <tr key={idx}>
                                        <td>{capitalizeCategory(cat)}</td>
                                        <td>{amt}</td>
                                        <td>{summary.expensesPercentage[cat]}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        <div className="mt-4 text-center">
                            <strong>Total Expenses:</strong> {summary.totalExpenses}
                            <br />
                            <strong>Highest Category:</strong> {capitalizeCategory(summary.highestCategory)}
                        </div>
                    </div>
                )}
            </div>
        </>
    );
}

export default ExpenseSummaryPage;