import { useEffect, useState, useCallback } from 'react';
import Header from '../components/Header.jsx';
import API_BASE_URL from '../config';

function SettingsPage({ accountId, onLogout }) {
    const [currency, setCurrency] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [editMode, setEditMode] = useState(false);
    const [newCurrency, setNewCurrency] = useState('');

    const fetchCurrency = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            const token = localStorage.getItem('token');
            const res = await fetch(`${API_BASE_URL}/api/settings/currency?accountId=${accountId}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (!res.ok) throw new Error(await res.text());
            const data = await res.json();
            setCurrency(data.currency);
        } catch (err) {
            setError(err.message || 'Failed to fetch currency.');
        }
        setLoading(false);
    }, [accountId]);

    useEffect(() => {
        fetchCurrency();
    }, [fetchCurrency]);

    const handleChangeCurrency = async (e) => {
        e.preventDefault();
        setError('');
        try {
            const token = localStorage.getItem('token');
            const payload = {
                accountId,
                newCurrency: newCurrency
            };
            const res = await fetch(`${API_BASE_URL}/api/settings/currency`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(payload),
            });
            if (!res.ok) throw new Error(await res.text());
            setCurrency(newCurrency.toUpperCase());
            setEditMode(false);
        } catch (err) {
            setError(err.message || 'Failed to update currency.');
        }
    };

    function displayCurrencyName(currency) {
        if (!currency) return '';
        if (currency === '$' || currency.toLowerCase() === 'dollar') return 'Dollar';
        if (currency === '€' || currency.toLowerCase() === 'euro') return 'Euro';
        return currency;
    }

    return (
        <>
            <Header onLogout={onLogout} />
            <div className="page-container">
                <h2 className="mb-4 text-center">Settings</h2>
                <hr className="mb-4" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                {loading && <div className="text-center">Loading...</div>}
                {error && <div className="alert alert-danger text-center">{error}</div>}
                {!loading && (
                    <div className="mx-auto" style={{ maxWidth: 400 }}>
                        <div className="mb-3">
                            <strong>Current Currency:</strong> {displayCurrencyName(currency)}
                        </div>
                        {!editMode ? (
                            <button
                                className="btn btn-dark"
                                style={{ borderRadius: 0 }}
                                onClick={() => setEditMode(true)}
                            >
                                Change Currency
                            </button>
                        ) : (
                            <form onSubmit={handleChangeCurrency}>
                                <div className="mb-3">
                                    <label className="form-label">Select Currency</label>
                                    <select
                                        className="form-select"
                                        value={newCurrency}
                                        onChange={e => setNewCurrency(e.target.value)}
                                        style={{ borderRadius: 0 }}
                                        required
                                    >
                                        <option value="">Choose...</option>
                                        <option value="euro">Euro</option>
                                        <option value="dollar">Dollar</option>
                                    </select>
                                </div>
                                <div className="d-flex justify-content-end">
                                    <button
                                        type="submit"
                                        className="btn btn-dark me-2"
                                        style={{ borderRadius: 0 }}
                                    >
                                        Save
                                    </button>
                                    <button
                                        type="button"
                                        className="btn btn-outline-secondary"
                                        style={{ borderRadius: 0 }}
                                        onClick={() => setEditMode(false)}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        )}
                    </div>
                )}
            </div>
        </>
    );
}

export default SettingsPage;