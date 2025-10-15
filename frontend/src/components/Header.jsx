import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useState, useRef, useEffect } from 'react';
import { FaBars, FaTimes } from 'react-icons/fa';

function Header({ onLogout }) {
    const navigate = useNavigate();
    const location = useLocation();
    const [menuOpen, setMenuOpen] = useState(false);
    const menuRef = useRef(null);

    // Helper to check if any child route is active
    const isReportsActive = ['/report-summary', '/expense-summary'].includes(location.pathname);
    const isTransactionsActive = ['/transactions', '/transactions/add', '/transaction-history'].includes(location.pathname);
    const isAccountActive = ['/account-manager', '/settings'].includes(location.pathname);

    // UseEffect to handle outside click
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setMenuOpen(false); // Close the menu when clicked outside
            }
        };

        document.addEventListener('mousedown', handleClickOutside);

        // Cleanup event listener
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [menuRef, setMenuOpen]);

    // UseEffect to close the menu when resizing the screen
    useEffect(() => {
        const handleResize = () => {
            if (window.innerWidth > 768) {
                setMenuOpen(false);
            }
        };

        window.addEventListener('resize', handleResize);

        // Cleanup the event listener on component unmount
        return () => {
            window.removeEventListener('resize', handleResize);
        };
    }, []);

    return (
        <div
            className="page-header"
            style={{
                position: 'sticky',
                top: 0,
                zIndex: 1200,
                background: '#fff',
            }}
        >
            {/* Mobile Header */}
            <div
                className="d-flex d-md-none align-items-center justify-content-between px-3 py-2"
                style={{ padding: '1rem', maxWidth: '1000px', margin: 'auto', marginBottom: '14px' }}
            >
                {/* Logo aligned left */}
                <div style={{ cursor: 'pointer' }} onClick={() => {
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                    if (location.pathname === '/balance') {
                        window.location.reload();
                    } else {
                        navigate('/balance');
                    }
                }}>
                    <img src="/logo.png" alt="Logo" style={{ height: 70 }} />
                </div>
                {/* Hamburger */}
                <button
                    className="btn"
                    style={{ background: 'none', border: 'none', padding: 0 }}
                    onClick={() => setMenuOpen(!menuOpen)}
                    aria-label="Open navigation"
                >
                    {menuOpen ? <FaTimes size={28} /> : <FaBars size={28} />}
                </button>
            </div>
            {/* Mobile Navigation Drawer */}
            <div
                ref={menuRef}
                className={`mobile-nav-drawer${menuOpen ? '' : ' closed'}`}
            >
                <nav>
                    <ul className="mobile-nav-list">
                        <li>
                            <NavLink className="mobile-nav-link" to="/balance" onClick={() => setMenuOpen(false)}>Dashboard</NavLink>
                        </li>
                        <li>
                            <NavLink className="mobile-nav-link" to="/transactions" onClick={() => setMenuOpen(false)}>Transaction Manager</NavLink>
                        </li>
                        <li>
                            <NavLink className="mobile-nav-link" to="/transactions/add" onClick={() => setMenuOpen(false)}>Add Transaction</NavLink>
                        </li>
                        <li>
                            <NavLink className="mobile-nav-link" to="/transaction-history" onClick={() => setMenuOpen(false)}>Transaction History</NavLink>
                        </li>
                        <li>
                            <NavLink className="mobile-nav-link" to="/report-summary" onClick={() => setMenuOpen(false)}>Report Summary</NavLink>
                        </li>
                        <li>
                            <NavLink className="mobile-nav-link" to="/expense-summary" onClick={() => setMenuOpen(false)}>Expense Summary</NavLink>
                        </li>
                        <li>
                            <NavLink className="mobile-nav-link" to="/account-manager" onClick={() => setMenuOpen(false)}>Account Manager</NavLink>
                        </li>
                        <li>
                            <NavLink className="mobile-nav-link" to="/settings" onClick={() => setMenuOpen(false)}>Settings</NavLink>
                        </li>
                        <li>
                            <NavLink className="mobile-nav-link" to="/data-storage" onClick={() => setMenuOpen(false)}>Data Storage</NavLink>
                        </li>
                    </ul>
                </nav>
                <div className="mobile-nav-logout">
                    <button
                        className="btn btn-dark"
                        onClick={() => { setMenuOpen(false); onLogout(); }}
                    >
                        Logout
                    </button>
                </div>
            </div>
            {/* Desktop Header */}
            <div className="d-none d-md-flex flex-column flex-md-row justify-content-center align-items-center mb-3" style={{ padding: '1rem', maxWidth: '1000px', margin: 'auto' }}>
                {/* Logo */}
                <div className="logo-bar me-3" style={{ cursor: 'pointer' }} onClick={() => {
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                    if (location.pathname === '/balance') {
                        window.location.reload();
                    } else {
                        navigate('/balance');
                    }
                }}>
                    <img src="/logo.png" alt="Logo" className="logo-img" />
                </div>
                {/* Navigation */}
                <nav className="navbar navbar-expand-md flex-grow-1 mx-3" style={{ borderRadius: 0, background: 'transparent' }}>
                    <div className="container-fluid p-0 justify-content-center">
                        <ul className="navbar-nav mx-auto mb-2 mb-lg-0 gap-3">
                            <li className="nav-item">
                                <NavLink className="nav-link" to="/balance">Dashboard</NavLink>
                            </li>
                            <li className="nav-item dropdown">
                                <button
                                    className={`nav-link dropdown-toggle btn btn-link${isTransactionsActive ? " active" : ""}`}
                                    id="transactionsDropdown"
                                    type="button"
                                    data-bs-toggle="dropdown"
                                    aria-expanded="false"
                                    style={{ textDecoration: 'none' }}
                                >
                                    Transactions
                                </button>
                                <ul className="dropdown-menu" aria-labelledby="transactionsDropdown" style={{ borderRadius: 0 }}>
                                    <li>
                                        <NavLink className={({ isActive }) => "dropdown-item" + (isActive ? " active" : "")} to="/transactions">Transaction Manager</NavLink>
                                    </li>
                                    <li>
                                        <NavLink className={({ isActive }) => "dropdown-item" + (isActive ? " active" : "")} to="/transactions/add">Add Transaction</NavLink>
                                    </li>
                                    <li>
                                        <NavLink className={({ isActive }) => "dropdown-item" + (isActive ? " active" : "")} to="/transaction-history">Transaction History</NavLink>
                                    </li>
                                </ul>
                            </li>
                            <li className="nav-item dropdown">
                                <button
                                    className={`nav-link dropdown-toggle btn btn-link${isReportsActive ? " active" : ""}`}
                                    id="reportsDropdown"
                                    type="button"
                                    data-bs-toggle="dropdown"
                                    aria-expanded="false"
                                    style={{ textDecoration: 'none' }}
                                >
                                    Reports
                                </button>
                                <ul className="dropdown-menu" aria-labelledby="reportsDropdown" style={{ borderRadius: 0 }}>
                                    <li>
                                        <NavLink className={({ isActive }) => "dropdown-item" + (isActive ? " active" : "")} to="/report-summary">Report Summary</NavLink>
                                    </li>
                                    <li>
                                        <NavLink className={({ isActive }) => "dropdown-item" + (isActive ? " active" : "")} to="/expense-summary">Expense Summary</NavLink>
                                    </li>
                                </ul>
                            </li>
                            <li className="nav-item dropdown">
                                <button
                                    className={`nav-link dropdown-toggle btn btn-link${isAccountActive ? " active" : ""}`}
                                    id="accountDropdown"
                                    type="button"
                                    data-bs-toggle="dropdown"
                                    aria-expanded="false"
                                    style={{ textDecoration: 'none' }}
                                >
                                    Account
                                </button>
                                <ul className="dropdown-menu" aria-labelledby="accountDropdown" style={{ borderRadius: 0 }}>
                                    <li>
                                        <NavLink className={({ isActive }) => "dropdown-item" + (isActive ? " active" : "")} to="/account-manager">Account Manager</NavLink>
                                    </li>
                                    <li>
                                        <NavLink className={({ isActive }) => "dropdown-item" + (isActive ? " active" : "")} to="/settings">Settings</NavLink>
                                    </li>
                                </ul>
                            </li>
                            <li className="nav-item">
                                <NavLink className="nav-link" to="/data-storage">Data Storage</NavLink>
                            </li>
                        </ul>
                    </div>
                </nav>
                {/* Logout Button for desktop only */}
                <button
                    className="btn btn-dark ms-3 d-none d-md-block"
                    style={{ borderRadius: 0 }}
                    onClick={onLogout}
                >
                    Logout
                </button>
            </div>
        </div>
    );
}

export default Header;