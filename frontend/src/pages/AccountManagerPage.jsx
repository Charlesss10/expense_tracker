import { useEffect, useState, useCallback } from 'react';
import Header from '../components/Header.jsx';
import 'bootstrap-icons/font/bootstrap-icons.css';
import API_BASE_URL from '../config.js';

function AccountManagerPage({ accountId, onLogout }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [editing, setEditing] = useState(false);
    const [form, setForm] = useState({});
    const [showPassword, setShowPassword] = useState(false);

    const fetchUser = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            const token = localStorage.getItem('token');
            const res = await fetch(`${API_BASE_URL}/api/users/${accountId}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (!res.ok) throw new Error(await res.text());
            const data = await res.json();
            setUser(data);
            setForm(data);
        } catch (err) {
            setError(err.message || 'Failed to fetch user info.');
        }
        setLoading(false);
    }, [accountId]);

    useEffect(() => {
        fetchUser();
    }, [fetchUser]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });
    };

    const handleEdit = () => {
        setEditing(true);
    };

    const handleCancel = () => {
        setForm(user);
        setEditing(false);
        setShowPassword(false);
    };

    const handleSave = async (e) => {
        e.preventDefault();
        try {
            const token = localStorage.getItem('token');
            const payload = {
                firstName: form.firstName || '',
                lastName: form.lastName || '',
                username: form.username || '',
                birthday: form.birthday || '',
                password: form.password || '',
                email: form.email || ''
            };
            const res = await fetch(`${API_BASE_URL}/api/users?accountId=${user.accountId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(payload),
            });
            if (!res.ok) throw new Error(await res.text());
            setUser({ ...user, ...payload });
            setEditing(false);
            setShowPassword(false);
        } catch (err) {
            alert('Failed to update account: ' + err.message);
        }
    };

    return (
        <>
            <Header onLogout={onLogout} />
            <div className="page-container">

                <h2 className="mb-4 text-center">Account Manager</h2>
                <hr className="mb-4" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                {loading && <div className="text-center">Loading...</div>}
                {error && <div className="alert alert-danger text-center">{error}</div>}
                {!loading && user && !editing && (
                    <div className="mx-auto" style={{ maxWidth: 500, borderRadius: 0 }}>
                        <div className="mb-3"><strong>First Name:</strong> {user.firstName}</div>
                        <div className="mb-3"><strong>Last Name:</strong> {user.lastName}</div>
                        <div className="mb-3"><strong>Username:</strong> {user.username}</div>
                        <div className="mb-3"><strong>Birthday:</strong> {user.birthday ? user.birthday.substring(0, 10) : ''}</div>
                        <div className="mb-3"><strong>Email:</strong> {user.email}</div>
                        <div className="d-flex justify-content-end">
                            <button
                                type="button"
                                className="btn btn-dark"
                                style={{ borderRadius: 0 }}
                                onClick={handleEdit}
                            >
                                Edit
                            </button>
                        </div>
                    </div>
                )}
                {!loading && user && editing && (
                    <form className="mx-auto" style={{ maxWidth: 500, borderRadius: 0 }} onSubmit={handleSave} >
                        <div className="mb-3">
                            <label className="form-label">First Name</label>
                            <input
                                type="text"
                                className="form-control"
                                name="firstName"
                                value={form.firstName || ''}
                                onChange={handleChange}
                                style={{ borderRadius: 0 }}
                            />
                        </div>
                        <div className="mb-3">
                            <label className="form-label">Last Name</label>
                            <input
                                type="text"
                                className="form-control"
                                name="lastName"
                                value={form.lastName || ''}
                                onChange={handleChange}
                                style={{ borderRadius: 0 }}
                            />
                        </div>
                        <div className="mb-3">
                            <label className="form-label">Username</label>
                            <input
                                type="text"
                                className="form-control"
                                name="username"
                                value={form.username || ''}
                                onChange={handleChange}
                                style={{ borderRadius: 0 }}
                                required
                            />
                        </div>
                        <div className="mb-3">
                            <label className="form-label">Birthday</label>
                            <input
                                type="date"
                                className="form-control"
                                name="birthday"
                                value={form.birthday ? form.birthday.substring(0, 10) : ''}
                                onChange={handleChange}
                                style={{ borderRadius: 0 }}
                            />
                        </div>
                        <div className="mb-3">
                            <label className="form-label">Email</label>
                            <input
                                type="email"
                                className="form-control"
                                name="email"
                                value={form.email || ''}
                                onChange={handleChange}
                                style={{ borderRadius: 0 }}
                                required
                            />
                        </div>
                        <div className="mb-3 position-relative">
                            <label className="form-label">Password (leave blank to keep unchanged)</label>
                            <input
                                type={showPassword ? "text" : "password"}
                                className="form-control"
                                name="password"
                                value={form.password || ''}
                                onChange={handleChange}
                                style={{ borderRadius: 0 }}
                            />
                            <span
                                className={`bi ${showPassword ? "bi-eye-slash" : "bi-eye"} position-absolute`}
                                style={{
                                    right: '10px',
                                    top: '38px',
                                    cursor: 'pointer',
                                    fontSize: '1.2rem',
                                    color: '#222'
                                }}
                                onMouseDown={() => setShowPassword(true)}
                                onMouseUp={() => setShowPassword(false)}
                                onMouseLeave={() => setShowPassword(false)}
                                title={showPassword ? "Hide password" : "Show password"}
                            ></span>
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
                                onClick={handleCancel}
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </>
    );
}

export default AccountManagerPage;