import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import 'bootstrap-icons/font/bootstrap-icons.css';
import API_BASE_URL from '../config';

function LoginPage({ onLogin }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [showPassword, setShowPassword] = useState(false);

    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const res = await axios.post(`${API_BASE_URL}/api/auth/login`, {
                username,
                password
            });
            if (res.data.token && res.data.accountId) {
                onLogin(res.data.token, res.data.accountId);
                navigate('/balance');
            } else {
                setError('Invalid username or password.');
            }
        } catch (err) {
            setError('Login failed. Please try again.');
        }
        setLoading(false);
    };

    const handleForgotPassword = () => {
        navigate('/reset-password');
    };

    return (
        <div className="page-container">
            <div className="login-container">
                <div className="card login-card" style={{ borderRadius: 0 }}>
                    <div className="card-body">
                        <h2 className="login-title">Expense Tracker Login</h2>
                        <hr className="mb-5" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                        {error && <div className="alert alert-danger" style={{ borderRadius: 0 }}>{error}</div>}
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label htmlFor="username" className="form-label login-label">Username</label>
                                <input
                                    type="text"
                                    className="form-control login-input"
                                    id="username"
                                    value={username}
                                    onChange={e => setUsername(e.target.value)}
                                    required
                                    autoFocus
                                    style={{ borderRadius: 0 }}
                                    placeholder="Enter your username"
                                />
                            </div>
                            <div className="mb-3 position-relative">
                                <label htmlFor="password" className="form-label login-label">Password</label>
                                <input
                                    type={showPassword ? "text" : "password"}
                                    className="form-control login-input"
                                    id="password"
                                    value={password}
                                    onChange={e => setPassword(e.target.value)}
                                    required
                                    style={{ borderRadius: 0 }}
                                    placeholder="Enter your password"
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
                            <button
                                type="submit"
                                className="btn btn-dark btn-lg w-100"
                                disabled={loading}
                                style={{ borderRadius: 0 }}
                            >
                                {loading ? 'Logging in...' : 'Login'}
                            </button>
                        </form>
                        <div className="mt-3 text-center">
                            <button
                                type="button"
                                className="btn btn-outline-dark"
                                style={{ borderRadius: 0, width: '100%' }}
                                onClick={() => navigate('/create-account')}
                            >
                                Create Account
                            </button>
                        </div>
                        <div className="mt-3 text-center">
                            <button
                                type="button"
                                className="btn btn-link forgot-link"
                                onClick={handleForgotPassword}
                            >
                                Forgot Password?
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default LoginPage;