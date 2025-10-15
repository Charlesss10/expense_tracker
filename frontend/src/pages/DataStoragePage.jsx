import { useState, useRef, useEffect } from 'react';
import Header from '../components/Header.jsx';
import API_BASE_URL from '../config';

function DataStoragePage({ accountId, onLogout }) {
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const fileInputRef = useRef();
    const [mode, setMode] = useState('');

    // Load Data: Upload file to backend
    const handleLoadData = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage('');
        setError('');
        const file = fileInputRef.current.files[0];
        if (!file) {
            setError('Please select a file to upload.');
            setLoading(false);
            return;
        }
        try {
            const token = localStorage.getItem('token');
            const formData = new FormData();
            formData.append('file', file);
            formData.append('accountId', accountId);
            const res = await fetch(`${API_BASE_URL}/api/data/load`, {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
                body: formData,
            });
            const text = await res.text();
            if (!res.ok) throw new Error(text);
            setMessage(text);
        } catch (err) {
            setError(err.message || 'Operation failed.');
        }
        setLoading(false);
    };

    // Save Data: Download file from backend
    const handleSaveData = async () => {
        setLoading(true);
        setMessage('');
        setError('');
        try {
            const token = localStorage.getItem('token');
            const res = await fetch(`${API_BASE_URL}/api/data/save?accountId=${accountId}`, {
                method: 'POST',
                headers: { Authorization: `Bearer ${token}` },
            });
            if (!res.ok) throw new Error(await res.text());
            const blob = await res.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'transactions.csv';
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
            setMessage('File downloaded successfully.');
        } catch (err) {
            setError(err.message || 'Operation failed.');
        }
        setLoading(false);
    };

    useEffect(() => {
        if (message || error) {
            const timer = setTimeout(() => {
                setMessage('');
                setError('');
            }, 2000);
            return () => clearTimeout(timer);
        }
    }, [message, error]);


    return (
        <>
            <Header onLogout={onLogout} />
            <div className="page-container">
                <h2 className="mb-4 text-center">Data Storage</h2>
                <hr className="mb-4" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                <div className="mx-auto" style={{ maxWidth: 400 }}>
                    <div className="mb-4 text-center">
                        <label className="form-label me-2">Choose Action:</label>
                        <select
                            className="form-select d-inline-block w-auto"
                            value={mode}
                            onChange={e => setMode(e.target.value)}
                            style={{ borderRadius: 0 }}
                        >
                            <option value="">Select...</option>
                            <option value="load">Load Data (Upload)</option>
                            <option value="save">Save Data (Download)</option>
                        </select>
                    </div>
                    {mode === 'load' && (
                        <form onSubmit={handleLoadData}>
                            <div className="mb-3">
                                <label className="form-label">Load Data (Upload File)</label>
                                <input
                                    type="file"
                                    className="form-control"
                                    ref={fileInputRef}
                                    accept=".csv"
                                    style={{ borderRadius: 0 }}
                                />
                                <button
                                    type="button"
                                    className="btn btn-outline-secondary mt-2"
                                    style={{ borderRadius: 0, whiteSpace: 'nowrap', height: '38px' }}
                                    onClick={() => {
                                        const csvContent = "Type,Amount,Category,Source,Description,Date\n";
                                        const blob = new Blob([csvContent], { type: "text/csv" });
                                        const url = window.URL.createObjectURL(blob);
                                        const a = document.createElement('a');
                                        a.href = url;
                                        a.download = 'transactions_template.csv';
                                        document.body.appendChild(a);
                                        a.click();
                                        a.remove();
                                        window.URL.revokeObjectURL(url);
                                    }}
                                >
                                    Download Template
                                </button>
                            </div>
                            <button
                                type="submit"
                                className="btn btn-dark"
                                style={{ borderRadius: 0 }}
                                disabled={loading}
                            >
                                Load Data
                            </button>
                        </form>
                    )}
                    {mode === 'save' && (
                        <div className="mb-4 mt-4">
                            <button
                                className="btn btn-dark"
                                style={{ borderRadius: 0 }}
                                disabled={loading}
                                onClick={handleSaveData}
                            >
                                Save Data (Download)
                            </button>
                        </div>
                    )}
                    {loading && <div className="text-center" style={{ borderRadius: 0 }}>Processing...</div>}
                    {message && <div className="alert alert-success text-center" style={{ borderRadius: 0 }}>{message}</div>}
                    {error && <div className="alert alert-danger text-center" style={{ borderRadius: 0 }}>{error}</div>}
                </div>
            </div>
        </>
    );
}

export default DataStoragePage;