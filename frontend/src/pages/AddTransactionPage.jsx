import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Header from '../components/Header.jsx';
import API_BASE_URL from '../config.js';

function AddTransactionPage({ accountId, onLogout }) {
    const [form, setForm] = useState({
        amount: '',
        category: '',
        date: '',
        description: '',
        type: '', // "expenses" or "income"
    });
    const incomeSources = [
        "Salary/Wages",
        "Business Income",
        "Freelance/Consulting",
        "Rental Income",
        "Investment Income",
        "Royalties",
        "Government Benefits",
        "Inheritance/Gifts",
        "Other"
    ];
    const expenseCategories = [
        "Housing (Rent/Mortgage)",
        "Utilities (Electricity, Water, Internet)",
        "Groceries",
        "Transportation",
        "Health (Insurance/Medical)",
        "Education",
        "Debt Repayment",
        "Entertainment",
        "Clothing",
        "Savings/Investments",
        "Gifts/Donations",
        "Other"
    ];
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

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
            const requestBody = {
                transaction: {
                    amount: form.amount,
                    category: form.type === "expenses" ? form.category : undefined,
                    source: form.type === "income" ? form.source : undefined,
                    date: form.date,
                    description: form.description,
                },
                type: form.type,
                accountId: accountId
            };
            await axios.post(
                `${API_BASE_URL}/api/transactions`,
                requestBody,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            setSuccess('Transaction added successfully!');
            setTimeout(() => navigate('/transactions'), 1500);
        } catch (err) {
            setError(err.response?.data || 'Failed to add transaction.');
        }
        setLoading(false);
    };

    return (
        <>
            <Header onLogout={onLogout} />
            <div className="page-container">

                <h2 className="mb-4 text-center">Add Transaction</h2>
                <hr className="mb-5" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                <div className="row justify-content-center">
                    <div className="col-12 col-md-20 col-lg-12">
                        <form onSubmit={handleSubmit}>
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
                                >
                                    <option value="">Select type</option>
                                    <option value="expenses">Expenses</option>
                                    <option value="income">Income</option>
                                </select>
                            </div>
                            {/* Show Category only for Expense */}
                            {form.type === "expenses" && (
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
                            {/* Show Source only for Income */}
                            {form.type === "income" && (
                                <div className="mb-3">
                                    <label className="form-label">Source</label>
                                    <select
                                        className="form-control"
                                        name="source"
                                        value={form.source || ''}
                                        onChange={e => setForm({ ...form, source: e.target.value })}
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
                                {loading ? 'Adding...' : 'Add Transaction'}
                            </button>
                            {error && <div className="alert alert-danger mt-3" style={{ borderRadius: 0 }}>{error}</div>}
                            {success && <div className="alert alert-success mt-3" style={{ borderRadius: 0 }}>{success}</div>}
                        </form>
                    </div>
                </div>
            </div>
        </>
    );
}

export default AddTransactionPage;