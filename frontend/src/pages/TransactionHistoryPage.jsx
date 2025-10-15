import { useEffect, useState } from 'react';
import Header from '../components/Header.jsx';
import API_BASE_URL from '../config.js';

function TransactionHistoryPage({ accountId, onLogout }) {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [currency, setCurrency] = useState('');

    useEffect(() => {
        async function fetchHistory() {
            setLoading(true);
            setError('');
            try {
                const token = localStorage.getItem('token');
                const res = await fetch(
                    `${API_BASE_URL}/api/transactions/history?accountId=${accountId}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                if (!res.ok) throw new Error(await res.text());
                const data = await res.json();
                setTransactions(data.transactions || []);
                setCurrency(data.currency || '');
            } catch (err) {
                setError(err.message || 'Failed to fetch transaction history.');
            }
            setLoading(false);
        }
        fetchHistory();
    }, [accountId]);

    function capitalizeWord(word) {
        return word
            .toLowerCase()
            .replace(/(^|\s)\S/g, l => l.toUpperCase());
    }

    return (
        <>
            <Header onLogout={onLogout} />
            <div className="page-container">
                <h2 className="mb-4 text-center">Transaction History</h2>
                <hr className="mb-4" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                {loading && <div className="text-center">Loading...</div>}
                {error && <div className="alert alert-danger text-center">{error}</div>}
                {!loading && !error && (
                    <div>
                        {/* Desktop table */}
                        <div className="table-responsive d-none d-md-block">
                            <table className="table table-bordered table-striped">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Amount{currency ? ` (${currency})` : ''}</th>
                                        <th>Type</th>
                                        <th>Category/Source</th>
                                        <th>Description</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {transactions.length === 0 ? (
                                        <tr>
                                            <td colSpan={5} className="text-center text-muted">No transactions found.</td>
                                        </tr>
                                    ) : (
                                        transactions.map((tx, idx) => (
                                            <tr key={idx}>
                                                <td>{tx.date}</td>
                                                <td>{tx.amount}</td>
                                                <td>{capitalizeWord(tx.type)}</td>
                                                <td>{capitalizeWord(tx.type === 'EXPENSES' ? tx.category : tx.source)}</td>
                                                <td>{capitalizeWord(tx.description)}</td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>
                        {/* Mobile cards */}
                        <div className="d-block d-md-none">
                            {transactions.length === 0 ? (
                                <div className="text-center mt-3 text-muted">No transactions found.</div>
                            ) : (
                                transactions.map((tx, idx) => (
                                    <div key={idx} className="card mb-2 shadow-sm" style={{ width: '90%', margin: '0 auto' }}>
                                        <div className="card-body p-3">
                                            <div className="fw-bold mb-2">{tx.date}</div>
                                            <div>
                                                <span className="fw-semibold">Amount:</span> {currency}{tx.amount}
                                            </div>
                                            <div>
                                                <span className="fw-semibold">Type:</span> {tx.type}
                                            </div>
                                            <div>
                                                <span className="fw-semibold">Category/Source:</span> {tx.type === 'EXPENSES' ? tx.category : tx.source}
                                            </div>
                                            {tx.description && (
                                                <div>
                                                    <span className="fw-semibold">Description:</span> {tx.description}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                )}
            </div >
        </>
    );
}

export default TransactionHistoryPage;