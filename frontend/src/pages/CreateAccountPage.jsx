import { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import API_BASE_URL from '../config';

function CreateAccountPage() {
    const [form, setForm] = useState({
        firstName: '',
        lastName: '',
        username: '',
        birthday: '',
        password: '',
        email: '',
        currency: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess('');
        try {
            await axios.post(`${API_BASE_URL}/api/users`, form);
            setSuccess('Account created successfully! You can now log in.');
            setTimeout(() => navigate('/login'), 1500);
        } catch (err) {
            setError(err.response?.data || 'Failed to create account.');
        }
        setLoading(false);
    };

    return (
        <div className="page-container">
            <div className="login-container">
                <div className="card login-card" style={{ borderRadius: 0 }}>
                    <div className="card-body">
                        <h2 className="login-title">Create Account</h2>
                        <hr className="mb-5" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3" >
                                <label className="form-label">First Name</label>
                                <input type="text" style={{ borderRadius: 0 }} className="form-control" name="firstName" value={form.firstName} onChange={handleChange} required />
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Last Name</label>
                                <input type="text" style={{ borderRadius: 0 }} className="form-control" name="lastName" value={form.lastName} onChange={handleChange} required />
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Username</label>
                                <input type="text" style={{ borderRadius: 0 }} className="form-control" name="username" value={form.username} onChange={handleChange} required />
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Birthday</label>
                                <input type="date" style={{ borderRadius: 0 }} className="form-control" name="birthday" value={form.birthday} onChange={handleChange} required />
                            </div>
                            <div className="mb-3 position-relative">
                                <label className="form-label">Password</label>
                                <input
                                    type={showPassword ? "text" : "password"}
                                    style={{ borderRadius: 0 }}
                                    className="form-control"
                                    name="password"
                                    value={form.password}
                                    onChange={handleChange}
                                    required
                                />
                                <span
                                    className={`bi ${showPassword ? "bi-eye-slash" : "bi-eye"}`}
                                    style={{
                                        position: 'absolute',
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
                            <div className="mb-3">
                                <label className="form-label">Email</label>
                                <input type="email" style={{ borderRadius: 0 }} className="form-control" name="email" value={form.email} onChange={handleChange} required />
                            </div>
                            <div className="mb-3">
                                <label className="form-label">Currency</label>
                                <select
                                    className="form-control"
                                    style={{ borderRadius: 0 }}
                                    name="currency"
                                    value={form.currency}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="">Select currency</option>
                                    <option value="Dollar">Dollar</option>
                                    <option value="Euro">Euro</option>
                                </select>
                            </div>
                            <button type="submit" className="btn btn-dark w-100" disabled={loading} style={{ borderRadius: 0 }}>
                                {loading ? 'Creating...' : 'Create Account'}
                            </button>
                        </form>
                        {error && <div className="alert alert-danger mb-3" style={{ borderRadius: 0 }}>{error}</div>}
                        {success && <div className="alert alert-success mb-3" style={{ borderRadius: 0 }}>{success}</div>}
                        <div className="mt-3 text-center">
                            <button type="button" className="btn btn-link" onClick={() => navigate('/login')}>
                                Already have an account? Login
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default CreateAccountPage;