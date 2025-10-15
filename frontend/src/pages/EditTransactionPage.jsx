import { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import axios from 'axios';
import Header from '../components/Header.jsx';
import API_BASE_URL from '../config.js';

const incomeSources = [
    "Salary/Wages", "Business Income", "Freelance/Consulting", "Rental Income",
    "Investment Income", "Royalties", "Government Benefits", "Inheritance/Gifts", "Other"
];
const expenseCategories = [
    "Housing (Rent/Mortgage)", "Utilities (Electricity, Water, Internet)", "Groceries",
    "Transportation", "Health (Insurance/Medical)", "Education", "Debt Repayment",
    "Entertainment", "Clothing", "Savings/Investments", "Gifts/Donations", "Other"
];

function EditTransactionPage({ accountId, onLogout }) {
    const { transactionId } = useParams();
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const type = queryParams.get('type') || 'income'; // default if not present
    const [form, setForm] = useState({
        amount: '',
        category: '',
        source: '',
        date: '',
        description: '',
        type: ''
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        async function fetchTransaction() {
            setLoading(true);
            setError('');
            try {
                const token = localStorage.getItem('token');
                const res = await axios.get(
                    `${API_BASE_URL}/api/transactions/${transactionId}?type=${type}&accountId=${accountId}`,
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                const tx = res.data;
                const normalizedSource = incomeSources.find(
                    src => src.toLowerCase() === (tx.source || '').trim().toLowerCase()
                ) || '';
                const normalizedCategory = expenseCategories.find(
                    src => src.toLowerCase() === (tx.category || '').trim().toLowerCase()
                ) || '';
                setForm({
                    amount: tx.amount,
                    category: normalizedCategory || '',
                    source: normalizedSource || '',
                    date: tx.date,
                    description: tx.description || '',
                    type: tx.type
                });
            } catch (err) {
                setError('Failed to load transaction.');
            }
            setLoading(false);
        }
        fetchTransaction();
    }, [transactionId, accountId, type]);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess('');
        try {
            const token = localStorage.getItem('token');
            await axios.put(
                `${API_BASE_URL}/api/transactions?accountId=${accountId}`,
                {
                    transactionId,
                    amount: form.amount,
                    category: form.category,
                    source: form.source,
                    date: form.date,
                    description: form.description,
                    type: form.type
                },
                { headers: { Authorization: `Bearer ${token}` } }
            );
            setSuccess('Transaction updated successfully!');
            setTimeout(() => navigate('/transactions/recent'), 1500);
        } catch (err) {
            setError(err.response?.data || 'Failed to update transaction.');
        }
        setLoading(false);
    };

    return (
        <>
            <Header onLogout={onLogout} />
            <div className="page-container">
                <h2 className="mb-4 text-center">Edit Transaction</h2>
                <hr className="mb-5" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                <div className="row justify-content-center">
                    <div className="col-12 col-md-8 col-lg-6">
                        {loading ? (
                            <div className="text-center">Loading...</div>
                        ) : (
                            <form onSubmit={handleSubmit}>
                                {error && <div className="alert alert-danger mt-3" style={{ borderRadius: 0 }}>{error}</div>}
                                {success && <div className="alert alert-success mt-3" style={{ borderRadius: 0 }}>{success}</div>}
                                <div className="mb-3">
                                    <label className="form-label">Amount</label>
                                    <input
                                        type="number"
                                        className="form-control"
                                        name="amount"
                                        value={form.amount}
                                        onChange={handleChange}
                                        required
                                        min="0"
                                        step="any"
                                        style={{ borderRadius: 0 }}
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Date</label>
                                    <input
                                        type="date"
                                        className="form-control"
                                        name="date"
                                        value={form.date}
                                        onChange={handleChange}
                                        required
                                        style={{ borderRadius: 0 }}
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">Type</label>
                                    <select
                                        className="form-control"
                                        name="type"
                                        value={form.type}
                                        onChange={handleChange}
                                        required
                                        style={{ borderRadius: 0 }}
                                        disabled
                                    >
                                        <option value="EXPENSES">Expenses</option>
                                        <option value="INCOME">Income</option>
                                    </select>
                                </div>
                                {form.type === "EXPENSES" && (
                                    <div className="mb-3">
                                        <label className="form-label">Category</label>
                                        <select
                                            className="form-control"
                                            name="category"
                                            value={form.category}
                                            onChange={handleChange}
                                            required
                                            style={{ borderRadius: 0 }}
                                        >
                                            <option value="">Select category</option>
                                            {expenseCategories.map((cat, idx) => (
                                                <option key={idx} value={cat}>{cat}</option>
                                            ))}
                                        </select>
                                    </div>
                                )}
                                {form.type === "INCOME" && (
                                    <div className="mb-3">
                                        <label className="form-label">Source</label>
                                        <select
                                            className="form-control"
                                            name="source"
                                            value={form.source}
                                            onChange={handleChange}
                                            required
                                            style={{ borderRadius: 0 }}
                                        >
                                            <option value="">Select source</option>
                                            {incomeSources.map((src, idx) => (
                                                <option key={idx} value={src}>{src}</option>
                                            ))}
                                        </select>
                                    </div>
                                )}
                                <div className="mb-3">
                                    <label className="form-label">Description</label>
                                    <textarea
                                        className="form-control"
                                        name="description"
                                        value={form.description}
                                        onChange={handleChange}
                                        style={{ borderRadius: 0, minHeight: '60px', resize: 'vertical' }}
                                        rows={2}
                                        placeholder="Enter description (optional)"
                                    />
                                </div>
                                <button type="submit" className="btn btn-dark w-100" disabled={loading} style={{ borderRadius: 0 }}>
                                    {loading ? 'Updating...' : 'Update Transaction'}
                                </button>
                            </form>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
}

export default EditTransactionPage;