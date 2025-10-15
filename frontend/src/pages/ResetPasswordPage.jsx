import { useState } from 'react';
import axios from 'axios';
import API_BASE_URL from '../config';

function ResetPasswordPage() {
    const [email, setEmail] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const [showPassword, setShowPassword] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage('');
        try {
            const res = await axios.post(`${API_BASE_URL}/api/auth/reset-password`, {
                email,
                newPassword
            });
            setMessage(res.data || 'Password reset successful.');
            setTimeout(() => {
                window.location.href = '/login';
            }, 1500);
        } catch (err) {
            if (err.response && err.response.data) {
                setMessage(err.response.data);
            } else {
                setMessage('Error resetting password.');
            }
        }
        setLoading(false);
    };

    return (
        <div className="page-container">
            <div className="login-container">
                <div className="card login-card" style={{ borderRadius: 0}}>
                    <div className="card-body">
                        <h2 className="login-title">Reset Password</h2>
                        <hr className="mb-5" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                        {message && <div className="alert alert-info">{message}</div>}
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label htmlFor="email" className="form-label login-label">Email</label>
                                <input
                                    type="email"
                                    className="form-control login-input"
                                    id="email"
                                    value={email}
                                    onChange={e => setEmail(e.target.value)}
                                    required
                                    style={{ borderRadius: 0 }}
                                    placeholder="Enter your email"
                                />
                            </div>
                            <div className="mb-3 position-relative">
                                <label htmlFor="newPassword" className="form-label login-label">New Password</label>
                                <input
                                    type={showPassword ? "text" : "password"}
                                    className="form-control login-input"
                                    id="newPassword"
                                    value={newPassword}
                                    onChange={e => setNewPassword(e.target.value)}
                                    required
                                    style={{ borderRadius: 0 }}
                                    placeholder="Enter new password"
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
                                className="btn login-btn"
                                disabled={loading}
                                style={{ borderRadius: 0 }}
                            >
                                {loading ? 'Resetting...' : 'Reset Password'}
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ResetPasswordPage;