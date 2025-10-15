import { useNavigate } from 'react-router-dom';
import Header from '../components/Header.jsx';
import { useState, useEffect } from 'react';
import axios from 'axios';
import API_BASE_URL from '../config.js';

function TransactionManagerPage({ accountId, onLogout }) {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const [error, setError] = useState('');
    const [pendingDeleteId, setPendingDeleteId] = useState(null);
    const [success, setSuccess] = useState('');
    const [currency, setCurrency] = useState('');
    const [filters, setFilters] = useState({
        amountStart: '',
        amountEnd: '',
        dateStart: '',
        dateEnd: '',
        category: '',
        source: ''
    });

    const fetchTransactions = async (paramsObj = {}) => {
        setLoading(true);
        setError('');
        try {
            const token = localStorage.getItem('token');
            const params = new URLSearchParams({ accountId });
            // Preprocess filters
            Object.entries(paramsObj).forEach(([key, value]) => {
                if (value !== '') {
                    params.append(key, value);
                }
            });
            const res = await fetch(
                `${API_BASE_URL}/api/transactions/recent?${params.toString()}`,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (!res.ok) throw new Error(await res.text());
            const data = await res.json();
            setTransactions(data.transactions || []);
            setCurrency(data.currency || '');
        } catch (err) {
            setError(err.message || 'Failed to fetch transactions.');
        }
        setLoading(false);
    };

    const handleConfirmDelete = async (id) => {
        setLoading(true);
        setError('');
        try {
            const token = localStorage.getItem('token');
            await axios.delete(
                `${API_BASE_URL}/api/transactions/${id}?&accountId=${accountId}`,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            setSuccess('Transaction deleted successfully!');
            fetchTransactions(filters); // Refresh list

            // Hide success message after 2 seconds
            setTimeout(() => setSuccess(''), 2000);
        } catch (err) {
            setError('Failed to delete transaction.');
        }
        setLoading(false);
        setPendingDeleteId(null);
    };

    // Initial load: show last 6 months (backend default)
    useEffect(() => {
        fetchTransactions();
        // eslint-disable-next-line
    }, [accountId]);


    const handleFilterChange = (e) => {
        setFilters({ ...filters, [e.target.name]: e.target.value });
    };

    const handleFilterSubmit = (e) => {
        e.preventDefault();
        fetchTransactions(filters);
    };

    const handleResetFilters = () => {
        const reset = {
            amountStart: '',
            amountEnd: '',
            dateStart: '',
            dateEnd: '',
            category: '',
            source: ''
        };
        setFilters(reset);
        fetchTransactions({ amountStart: 0.0, amountEnd: 0.0 });
    };

    function capitalizeWord(word) {
        return word
            .toLowerCase()
            .replace(/(^|\s)\S/g, l => l.toUpperCase());
    }

    return (
        <>
            <Header onLogout={onLogout} />

            <div className="page-container">
                <h2 className="mb-4 text-center">Transaction Manager</h2>
                <hr className="mb-4" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                <div className="row justify-content-center">
                    {/* Add Transaction Button */}
                    <div className="mb-3 col-12">
                        <button
                            className="btn btn-dark w-100 d-block d-md-none"
                            style={{ borderRadius: 0 }}
                            onClick={() => {
                                window.scrollTo({ top: 0, behavior: 'smooth' });
                                navigate('/transactions/add');
                            }}
                        >
                            Add Transaction
                        </button>
                        <button
                            className="btn btn-dark d-none d-md-inline-block"
                            style={{ borderRadius: 0, minWidth: 180 }}
                            onClick={() => {
                                window.scrollTo({ top: 0, behavior: 'smooth' });
                                navigate('/transactions/add');
                            }}
                        >
                            Add Transaction
                        </button>
                    </div>
                    {/* Recent Transactions Header */}
                    <div className="mb-2 mt-3">
                        <h5 className=" text-center">Recent Transactions</h5>
                    </div>
                    {/* Filter Form */}
                    <form className="mb-3" onSubmit={handleFilterSubmit}>
                        <div className="row g-2 flex-column flex-md-row">
                            <div className="col mb-2">
                                <input
                                    style={{ borderRadius: 0 }}
                                    type="number"
                                    className="form-control"
                                    name="amountStart"
                                    value={filters.amountStart}
                                    onChange={handleFilterChange}
                                    placeholder="Start Amount"
                                    step="any"
                                />
                            </div>
                            <div className="col mb-2">
                                <input
                                    style={{ borderRadius: 0 }}
                                    type="number"
                                    className="form-control"
                                    name="amountEnd"
                                    value={filters.amountEnd}
                                    onChange={handleFilterChange}
                                    placeholder="End Amount"
                                    step="any"
                                />
                            </div>
                            <div className="col mb-2">
                                <input
                                    style={{ borderRadius: 0 }}
                                    type="date"
                                    className="form-control date-placeholder"
                                    name="dateStart"
                                    value={filters.dateStart}
                                    onChange={handleFilterChange}
                                    placeholder="Date Start"
                                />
                            </div>
                            <div className="col mb-2">
                                <input
                                    style={{ borderRadius: 0 }}
                                    type="date"
                                    className="form-control date-placeholder"
                                    name="dateEnd"
                                    value={filters.dateEnd}
                                    onChange={handleFilterChange}
                                    placeholder="Date End"
                                />
                            </div>
                            <div className="col mb-2">
                                <input
                                    style={{ borderRadius: 0 }}
                                    type="text"
                                    className="form-control"
                                    name="category"
                                    value={filters.category}
                                    onChange={handleFilterChange}
                                    placeholder="Category"
                                />
                            </div>
                            <div className="col mb-2">
                                <input
                                    style={{ borderRadius: 0 }}
                                    type="text"
                                    className="form-control"
                                    name="source"
                                    value={filters.source}
                                    onChange={handleFilterChange}
                                    placeholder="Source"
                                />
                            </div>
                            <div className="col-12 mb-2">
                                {/* Mobile: full width, grid spacing */}
                                <div className="d-grid gap-2 d-md-none">
                                    <button
                                        style={{ borderRadius: 0 }}
                                        type="submit"
                                        className="btn btn-dark w-100"
                                    >
                                        Filter
                                    </button>
                                    <button
                                        style={{ borderRadius: 0 }}
                                        type="button"
                                        className="btn btn-outline-secondary w-100"
                                        onClick={handleResetFilters}
                                    >
                                        Reset
                                    </button>
                                </div>
                                {/* Desktop: left aligned, fixed width */}
                                <div className="d-none d-md-flex gap-2">
                                    <button
                                        style={{ borderRadius: 0, minWidth: 180 }}
                                        type="submit"
                                        className="btn btn-dark"
                                    >
                                        Filter
                                    </button>
                                    <button
                                        style={{ borderRadius: 0, minWidth: 180 }}
                                        type="button"
                                        className="btn btn-outline-secondary"
                                        onClick={handleResetFilters}
                                    >
                                        Reset
                                    </button>
                                </div>
                            </div>
                        </div>
                    </form>
                    {loading && <div className="text-center" style={{ borderRadius: 0 }}>Loading...</div>}
                    {error && <div className="alert alert-danger text-center" style={{ borderRadius: 0 }}>{error}</div>}
                    {success && <div className="alert alert-success text-center" style={{ borderRadius: 0 }}>{success}</div>}
                    {!loading && !error && (
                        <div className="table-responsive">
                            <div className="table-responsive d-none d-md-block">
                                <table className="table table-bordered table-striped">
                                    <thead>
                                        <tr>
                                            <th>Date</th>
                                            <th>
                                                Amount
                                                {currency ? ` (${currency})` : ''}
                                            </th>
                                            <th>Type</th>
                                            <th>Category/Source</th>
                                            {/* Hide Description on mobile */}
                                            <th className="d-none d-md-table-cell">Description</th>
                                            {/* Move actions to a single column, hide on mobile */}
                                            <th className="d-none d-md-table-cell">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {transactions.length === 0 ? (
                                            <tr>
                                                <td colSpan="5" className="text-center">No transactions found.</td>
                                            </tr>
                                        ) : (
                                            transactions.map((tx, idx) => (
                                                <tr key={idx}>
                                                    <td>{tx.date}</td>
                                                    <td>{tx.amount}</td>
                                                    <td>{capitalizeWord(tx.type)}</td>
                                                    <td>{tx.type === 'EXPENSES' ? capitalizeWord(tx.category) : capitalizeWord(tx.source)}</td>
                                                    {/* Hide Description on mobile */}
                                                    <td className="d-none d-md-table-cell">{capitalizeWord(tx.description)}</td>
                                                    {/* Desktop actions */}
                                                    <td className="d-none d-md-table-cell">
                                                        <button
                                                            className="btn btn-outline-dark btn-sm"
                                                            style={{ borderRadius: 0, marginRight: '8px' }}
                                                            onClick={() => navigate(`/transactions/edit/${tx.transactionId}?type=${tx.type}`)}
                                                        >
                                                            Edit
                                                        </button>
                                                        {pendingDeleteId === tx.transactionId ? (
                                                            <>
                                                                <button
                                                                    className="btn btn-danger btn-sm"
                                                                    style={{ borderRadius: 0, marginRight: '8px' }}
                                                                    onClick={() => handleConfirmDelete(tx.transactionId)}
                                                                >
                                                                    Sure?
                                                                </button>
                                                                <button
                                                                    className="btn btn-secondary btn-sm"
                                                                    style={{ borderRadius: 0 }}
                                                                    onClick={() => setPendingDeleteId(null)}
                                                                >
                                                                    Cancel
                                                                </button>
                                                            </>
                                                        ) : (
                                                            <button
                                                                className="btn btn-outline-danger btn-sm"
                                                                style={{ borderRadius: 0 }}
                                                                onClick={() => setPendingDeleteId(tx.transactionId)}
                                                            >
                                                                Delete
                                                            </button>
                                                        )}
                                                    </td>
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </table>
                            </div>
                            {/* Mobile cards: show only on mobile */}
                            <div className="d-block d-md-none">
                                {transactions.length === 0 ? (
                                    <div className="text-center mt-3">No transactions found.</div>
                                ) : (
                                    transactions.map((tx, idx) => (
                                        <div key={idx} className="card mb-2 shadow-sm">
                                            <div className="card-body p-3">
                                                <div className="fw-bold mb-2">{tx.date}</div>
                                                <div>
                                                    <span className="fw-semibold">Amount:</span> {currency}{tx.amount}
                                                </div>
                                                <div>
                                                    <span className="fw-semibold">Type:</span> {tx.type}
                                                </div>
                                                <span className="fw-semibold">Category/Source:</span> {tx.type === 'EXPENSES' ? tx.category : tx.source}
                                                {tx.description && (
                                                    <div>
                                                        <span className="fw-semibold">Description:</span> {tx.description}
                                                    </div>
                                                )}
                                                <div className="mt-3 pt-2">
                                                    <button
                                                        className="btn btn-outline-dark btn-sm me-2"
                                                        style={{ borderRadius: 0 }}
                                                        onClick={() => navigate(`/transactions/edit/${tx.transactionId}?type=${tx.type}`)}
                                                    >
                                                        Edit
                                                    </button>
                                                    {pendingDeleteId === tx.transactionId ? (
                                                        <>
                                                            <button
                                                                className="btn btn-danger btn-sm me-2"
                                                                style={{ borderRadius: 0 }}
                                                                onClick={() => handleConfirmDelete(tx.transactionId)}
                                                            >
                                                                Sure?
                                                            </button>
                                                            <button
                                                                className="btn btn-secondary btn-sm"
                                                                style={{ borderRadius: 0 }}
                                                                onClick={() => setPendingDeleteId(null)}
                                                            >
                                                                Cancel
                                                            </button>
                                                        </>
                                                    ) : (
                                                        <button
                                                            className="btn btn-outline-danger btn-sm"
                                                            style={{ borderRadius: 0 }}
                                                            onClick={() => setPendingDeleteId(tx.transactionId)}
                                                        >
                                                            Delete
                                                        </button>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </>
    );
}

export default TransactionManagerPage;