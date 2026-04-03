import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header.jsx';
import { apiFetch } from '../api.js';

function TotalBalancePage({ accountId, onLogout }) {
    const [balance, setBalance] = useState(null);
    const [income, setIncome] = useState(null);
    const [expenses, setExpenses] = useState(null);
    const [currency, setCurrency] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [status, setStatus] = useState('Checking backend status...');
    const navigate = useNavigate();

    useEffect(() => {
        async function fetchBalance() {
            try {
                const res = await apiFetch('/api/transactions/balance', {
                    method: 'GET',
                });

                if (!res.ok) {
                    setError('Failed to fetch balance.');
                    setLoading(false);
                    return;
                }

                const data = await res.json();
                setBalance(data.totalBalance);
                setIncome(data.totalIncome);
                setExpenses(data.totalExpenses);
                setCurrency(data.currency);
                setStatus('Data loaded successfully.');
                setLoading(false);
            } catch (err) {
                setStatus('Backend is starting or connection is being established. Waiting...');
                setError('Failed to fetch balance. Please wait and try again.');
                setLoading(false);
            }
        }
        fetchBalance();
    }, [accountId, onLogout]);

    if (loading) return (
        <div className="text-center mt-5">
            <div className="spinner-border text-primary" role="status"><span className="visually-hidden">Loading...</span></div>
            <p className="mt-3" style={{ color: '#333', fontWeight: 600 }}>{status}</p>
            <p style={{ color: '#555' }}>If the page stays here, the backend may still be initializing (Render can take ~2 minutes after idle).</p>
        </div>
    );
    if (error) return <div className="alert alert-danger mt-5">{error}</div>;

    return (
        <>
            <Header onLogout={onLogout} />
            <div className="page-container">
                <h2 className="mb-4 text-center">Total Balance Overview</h2>
                <hr className="mb-5" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                <div className="row justify-content-center">
                    <div className="col-12 col-md-4 mb-3">
                        <div className="card border-success shadow">
                            <div className="card-body text-center">
                                <h5 className="card-title text-success">Total Balance</h5>
                                <p className="card-text fs-3 fw-bold">{currency}{balance}</p>
                            </div>
                        </div>
                    </div>
                    <div className="col-12 col-md-4 mb-3">
                        <div className="card border-primary shadow">
                            <div className="card-body text-center">
                                <h5 className="card-title text-primary" >Total Income</h5>
                                <p className="card-text fs-3 fw-bold">{currency}{income}</p>
                            </div>
                        </div>
                    </div>
                    <div className="col-12 col-md-4 mb-3">
                        <div className="card border-danger shadow">
                            <div className="card-body text-center">
                                <h5 className="card-title text-danger">Total Expenses</h5>
                                <p className="card-text fs-3 fw-bold">{currency}{expenses}</p>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="mt-5">
                    <div className="row justify-content-center">
                        <div className="d-grid gap-3">
                            <button
                                className="btn btn-lg btn-light w-100 shadow-sm border-0"
                                style={{
                                    borderRadius: '0.5rem',
                                    boxShadow: '0 2px 8px rgba(34,34,34,0.12)',
                                    fontWeight: 500,
                                    letterSpacing: '0.5px'
                                }}
                                onClick={() => navigate('/transactions')}
                            >
                                Transaction Manager
                            </button>
                            <button
                                className="btn btn-light btn-lg w-100 shadow-sm border-0"
                                style={{
                                    borderRadius: '0.5rem',
                                    boxShadow: '0 2px 8px rgba(34,34,34,0.12)',
                                    fontWeight: 500,
                                    letterSpacing: '0.5px'
                                }}
                                onClick={() => navigate('/expense-summary')}
                            >
                                View Expense Summary
                            </button>
                            <button
                                className="btn btn-light btn-lg w-100 shadow-sm border-0"
                                style={{
                                    borderRadius: '0.5rem',
                                    boxShadow: '0 2px 8px rgba(34,34,34,0.12)',
                                    fontWeight: 500,
                                    letterSpacing: '0.5px'
                                }}
                                onClick={() => navigate('/report-summary')}
                            >
                                Generate Report Summary
                            </button>
                            <button
                                className="btn btn-light btn-lg w-100 shadow-sm border-0"
                                style={{
                                    borderRadius: '0.5rem',
                                    boxShadow: '0 2px 8px rgba(34,34,34,0.12)',
                                    fontWeight: 500,
                                    letterSpacing: '0.5px'
                                }}
                                onClick={() => navigate('/transaction-history')}
                            >
                                View Transaction History
                            </button>
                            <button
                                className="btn btn-light btn-lg w-100 shadow-sm border-0"
                                style={{
                                    borderRadius: '0.5rem',
                                    boxShadow: '0 2px 8px rgba(34,34,34,0.12)',
                                    fontWeight: 500,
                                    letterSpacing: '0.5px'
                                }}
                                onClick={() => navigate('/data-storage')}
                            >
                                Data Storage
                            </button>
                            <button
                                className="btn btn-light btn-lg w-100 shadow-sm border-0"
                                style={{
                                    borderRadius: '0.5rem',
                                    boxShadow: '0 2px 8px rgba(34,34,34,0.12)',
                                    fontWeight: 500,
                                    letterSpacing: '0.5px'
                                }}
                                onClick={() => navigate('/account-manager')}
                            >
                                Account Manager
                            </button>
                            <button
                                className="btn btn-light btn-lg w-100 shadow-sm border-0"
                                style={{
                                    borderRadius: '0.5rem',
                                    boxShadow: '0 2px 8px rgba(34,34,34,0.12)',
                                    fontWeight: 500,
                                    letterSpacing: '0.5px'
                                }}
                                onClick={() => navigate('/settings')}
                            >
                                Settings
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}

export default TotalBalancePage;