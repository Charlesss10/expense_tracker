import './App.css';
import TotalBalancePage from './pages/TotalBalancePage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import ResetPasswordPage from './pages/ResetPasswordPage.jsx';
import { apiFetch } from './api.js';
import BootingUpScreen from './components/BootingUpScreen.jsx';
import { useState, useEffect } from 'react';
import TransactionManagerPage from './pages/TransactionManagerPage.jsx';
import ReportSummaryPage from './pages/ReportSummaryPage.jsx';
import DataStoragePage from './pages/DataStoragePage.jsx';
import AccountManagerPage from './pages/AccountManagerPage.jsx';
import SettingsPage from './pages/SettingsPage.jsx';
import CreateAccountPage from './pages/CreateAccountPage.jsx';
import AddTransactionPage from './pages/AddTransactionPage.jsx';
import EditTransactionPage from './pages/EditTransactionPage.jsx';
import ExpenseSummaryPage from './pages/ExpenseSummaryPage.jsx';
import TransactionHistoryPage from './pages/TransactionHistoryPage.jsx';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Global function to trigger booting screen
window.triggerBootingScreen = null;
window.resetBootingScreen = null;

function App() {
  const [accountId, setAccountId] = useState(() => localStorage.getItem('accountId'));
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [isBooting, setIsBooting] = useState(false);
  const [bootTimeLeft, setBootTimeLeft] = useState(120);

  // Set global function
  useEffect(() => {
    window.triggerBootingScreen = () => {
      setIsBooting(true);
      setBootTimeLeft(120);
    };
    window.resetBootingScreen = () => {
      setIsBooting(false);
      setBootTimeLeft(120);
    };
  }, []);

  useEffect(() => {
    let interval;
    if (isBooting && bootTimeLeft > 0) {
      interval = setInterval(() => {
        setBootTimeLeft(prev => prev - 1);
      }, 1000);
    } else if (bootTimeLeft === 0) {
      setIsBooting(false);
      setBootTimeLeft(120);
    }
    return () => clearInterval(interval);
  }, [isBooting, bootTimeLeft]);

  // Called after successful login
  const handleLogin = (token, accountId) => {
    setToken(token);
    setAccountId(accountId);
    localStorage.setItem('accountId', accountId);
    localStorage.setItem('token', token);
  };

  // Called on logout
  const handleLogout = async () => {
    const token = localStorage.getItem('token');
    const accountId = localStorage.getItem('accountId');

    if (!token || !accountId) {
      setToken(null);
      setAccountId(null);
      localStorage.removeItem('accountId');
      localStorage.removeItem('token');
      return;
    }

    try {
      await apiFetch(`/api/auth/logout?accountId=${encodeURIComponent(accountId)}`, {
        method: 'POST',
        includeAccountId: false,
      });
    } catch (err) {
      // silent auth cleanup on logout failure
    }

    setToken(null);
    setAccountId(null);
    localStorage.removeItem('accountId');
    localStorage.removeItem('token');
  };

  // Default route logic
  const isAuthenticated = token && accountId;

  return (
    <Router>
      <div className="App">
        {isBooting && <BootingUpScreen timeLeft={bootTimeLeft} />}
        <Routes>
          <Route
            path="/"
            element={
              isAuthenticated
                ? <Navigate to="/balance" replace />
                : <Navigate to="/login" replace />
            }
          />
          <Route
            path="/login"
            element={
              isAuthenticated
                ? <Navigate to="/balance" replace />
                : <LoginPage onLogin={handleLogin} />
            }
          />
          <Route
            path="/create-account"
            element={<CreateAccountPage />}
          />
          <Route
            path="/reset-password"
            element={<ResetPasswordPage />}
          />
          <Route
            path="/balance"
            element={
              isAuthenticated
                ? <TotalBalancePage accountId={accountId} onLogout={handleLogout} />
                : <Navigate to="/login" replace />
            }
          />
          <Route
            path="/transactions"
            element={
              isAuthenticated
                ? <TransactionManagerPage accountId={accountId} onLogout={handleLogout} />
                : <Navigate to="/login" replace />
            }
          />
          <Route
            path="/report-summary"
            element={
              isAuthenticated
                ? <ReportSummaryPage accountId={accountId} onLogout={handleLogout} />
                : <Navigate to="/login" replace />
            }
          />
          <Route
            path="/data-storage"
            element={
              isAuthenticated
                ? <DataStoragePage accountId={accountId} onLogout={handleLogout} />
                : <Navigate to="/login" replace />
            }
          />
          <Route
            path="/account-manager"
            element={
              isAuthenticated
                ? <AccountManagerPage accountId={accountId} onLogout={handleLogout} />
                : <Navigate to="/login" replace />
            }
          />
          <Route
            path="/settings"
            element={
              isAuthenticated
                ? <SettingsPage accountId={accountId} onLogout={handleLogout} />
                : <Navigate to="/login" replace />
            }
          />
          <Route
            path="/transactions/add"
            element={
              isAuthenticated
                ? <AddTransactionPage accountId={accountId} onLogout={handleLogout} />
                : <Navigate to="/login" replace />
            }
          />
          <Route
            path="/transactions/edit/:transactionId"
            element={
              isAuthenticated
                ? <EditTransactionPage accountId={accountId} onLogout={handleLogout} />
                : <Navigate to="/login" replace />
            }
          />
          <Route
            path="/expense-summary"
            element={
              isAuthenticated
                ? <ExpenseSummaryPage accountId={accountId} onLogout={handleLogout} />
                : <Navigate to="/login" replace />
            }
          />
          <Route
            path="/transaction-history"
            element={
              isAuthenticated
                ? <TransactionHistoryPage accountId={accountId} onLogout={handleLogout} />
                : <Navigate to="/login" replace />
            }
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;