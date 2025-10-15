import { useEffect, useState } from 'react';
import Header from '../components/Header.jsx';
import API_BASE_URL from '../config.js';

function capitalizeCategory(cat) {
    if (!cat) return '';
    return cat
        .toLowerCase()
        .replace(/(^|\s)\S/g, l => l.toUpperCase());
}

// Generate years for dropdown (e.g., last 10 years)
const currentYear = new Date().getFullYear();
const years = Array.from({ length: 10 }, (_, i) => `${currentYear - i}`);
const months = [
    { value: '01', label: 'January' },
    { value: '02', label: 'February' },
    { value: '03', label: 'March' },
    { value: '04', label: 'April' },
    { value: '05', label: 'May' },
    { value: '06', label: 'June' },
    { value: '07', label: 'July' },
    { value: '08', label: 'August' },
    { value: '09', label: 'September' },
    { value: '10', label: 'October' },
    { value: '11', label: 'November' },
    { value: '12', label: 'December' },
];

function ReportSummaryPage({ accountId, onLogout }) {
    const [report, setReport] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [filters, setFilters] = useState({ month: '', year: '' });
    const [filterType, setFilterType] = useState('month'); // 'month' or 'year'
    const [currency, setCurrency] = useState('');

    useEffect(() => {
        async function fetchReport() {
            setLoading(true);
            setError('');
            let endpoint = '';
            let params = `accountId=${accountId}`;
            if (filterType === 'month' && filters.month && filters.year) {
                endpoint = 'monthly';
                params += `&targetMonth=${filters.year}-${filters.month}`;
            } else if (filterType === 'year' && filters.year) {
                endpoint = 'yearly';
                params += `&targetYear=${filters.year}`;
            } else {
                endpoint = 'general';
            }
            try {
                const token = localStorage.getItem('token');
                const res = await fetch(
                    `${API_BASE_URL}/api/reports/${endpoint}?${params}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                if (!res.ok) throw new Error(await res.text());
                const data = await res.json();
                setReport(data);
                setCurrency(data.currency || '');
            } catch (err) {
                setError(err.message || 'Failed to fetch report summary.');
            }
            setLoading(false);
        }
        fetchReport();
    }, [accountId, filters, filterType]);

    // Export to CSV (server-side only, with filters)
    const handleServerExportCSV = async () => {
        try {
            const token = localStorage.getItem('token');
            let params = `accountId=${accountId}`;
            if (filterType === 'month' && filters.month && filters.year) {
                params += `&targetMonth=${filters.year}-${filters.month}`;
            } else if (filterType === 'year' && filters.year) {
                params += `&targetYear=${filters.year}`;
            }
            const res = await fetch(
                `${API_BASE_URL}/api/reports/export-csv?${params}`,
                {
                    method: 'POST',
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            if (!res.ok) throw new Error(await res.text());
            const blob = await res.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'report_summary.csv';
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        } catch (err) {
            setError('Failed to export CSV: ' + err.message);
        }
    };

    const handleFilterTypeChange = (e) => {
        setFilterType(e.target.value);
        setFilters({ month: '', year: '' }); // Reset filters when type changes
    };

    const handleFilterChange = (e) => {
        setFilters({ ...filters, [e.target.name]: e.target.value });
    };

    const handleResetFilters = () => {
        setFilters({ month: '', year: '' });
    };

    return (
        <>
            <Header onLogout={onLogout} />
            <div className="page-container">
                <h2 className="mb-4 text-center">Report Summary</h2>
                <hr className="mb-4" style={{ borderTop: '2px solid #222', width: '60%', margin: '0 auto' }} />
                {/* Filter Form */}
                <form className="mb-4" onSubmit={e => e.preventDefault()}>
                    <div className="row g-2 justify-content-center align-items-end">
                        <div className="col-auto">
                            <select
                                className="form-select"
                                value={filterType}
                                onChange={handleFilterTypeChange}
                                style={{ borderRadius: 0 }}
                            >
                                <option value="month">Monthly</option>
                                <option value="year">Yearly</option>
                            </select>
                        </div>
                        {filterType === 'month' && (
                            <>
                                <div className="col-auto">
                                    <select
                                        className="form-select"
                                        name="year"
                                        value={filters.year}
                                        onChange={handleFilterChange}
                                        style={{ borderRadius: 0 }}
                                    >
                                        <option value="">Select Year</option>
                                        {years.map(y => (
                                            <option key={y} value={y}>{y}</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="col-auto">
                                    <select
                                        className="form-select"
                                        name="month"
                                        value={filters.month}
                                        onChange={handleFilterChange}
                                        style={{ borderRadius: 0 }}
                                    >
                                        <option value="">Select Month</option>
                                        {months.map(m => (
                                            <option key={m.value} value={m.value}>{m.label}</option>
                                        ))}
                                    </select>
                                </div>
                            </>
                        )}
                        {filterType === 'year' && (
                            <div className="col-auto">
                                <select
                                    className="form-select"
                                    name="year"
                                    value={filters.year}
                                    onChange={handleFilterChange}
                                    style={{ borderRadius: 0 }}
                                >
                                    <option value="">Select Year</option>
                                    {years.map(y => (
                                        <option key={y} value={y}>{y}</option>
                                    ))}
                                </select>
                            </div>
                        )}
                        <div className="col-auto">
                            <button type="button" className="btn btn-dark" style={{ borderRadius: 0 }} onClick={handleResetFilters}>
                                Reset
                            </button>
                        </div>
                    </div>
                </form>
                {/* Active Filter Summary */}
                {filterType === 'month' && (!filters.month || !filters.year) && (
                    <div className="text-center text-muted mb-4">
                        Please select both <strong>month</strong> and <strong>year</strong> to view the monthly report.
                    </div>
                )}
                {filterType === 'month' && filters.month && filters.year && (
                    <div className="text-center text-muted mb-4">
                        Showing monthly report for <strong>{months.find(m => m.value === filters.month)?.label} {filters.year}</strong>
                    </div>
                )}
                {filterType === 'year' && !filters.year && (
                    <div className="text-center text-muted mb-4">
                        Please select a <strong>year</strong> to view the yearly report.
                    </div>
                )}
                {filterType === 'year' && filters.year && (
                    <div className="text-center mb-2 text-muted">
                        Showing yearly report for <strong>{filters.year}</strong>
                    </div>
                )}
                {loading && <div className="text-center">Loading...</div>}
                {error && <div className="alert alert-danger text-center" style={{ borderRadius: 0 }}>{error} </div>}
                {!loading && !error && report && (
                    ((filterType === 'month' && filters.month && filters.year) ||
                        (filterType === 'year' && filters.year)) && (
                        <div>
                            <div className="mb-4 text-center">
                                <strong>Total Income:</strong> {currency}{report.totalIncome}<br />
                                <strong>Total Expenses:</strong> {currency}{report.totalExpenses}<br />
                                <strong>Total Balance:</strong> {currency}{report.totalBalance}<br />
                                <strong>Highest Source:</strong> {capitalizeCategory(report.highestSource)}<br />
                                <strong>Highest Category:</strong> {capitalizeCategory(report.highestCategory)}
                            </div>
                            <div className="table-responsive mb-4">
                                <h5>Income by Source</h5>
                                <table className="table table-bordered table-striped">
                                    <thead>
                                        <tr>
                                            <th>Source</th>
                                            <th>Amount{currency ? ` (${currency})` : ''}</th>
                                            <th>Percentage (%)</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Object.entries(report.incomeBySource).map(([source, amount], idx) => (
                                            <tr key={idx}>
                                                <td>{capitalizeCategory(source)}</td>
                                                <td>{amount}</td>
                                                <td>{report.incomePercentage[source]}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                            <div className="table-responsive mb-4">
                                <h5>Expenses by Category</h5>
                                <table className="table table-bordered table-striped">
                                    <thead>
                                        <tr>
                                            <th>Category</th>
                                            <th>Amount{currency ? ` (${currency})` : ''}</th>
                                            <th>Percentage (%)</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Object.entries(report.expensesByCategory).map(([cat, amount], idx) => (
                                            <tr key={idx}>
                                                <td>{capitalizeCategory(cat)}</td>
                                                <td>{amount}</td>
                                                <td>{report.expensesPercentage[cat]}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                            <div className="text-center mb-4">
                                <button
                                    className="btn btn-outline-dark"
                                    style={{ borderRadius: 0 }}
                                    onClick={handleServerExportCSV}
                                >
                                    Export to CSV
                                </button>
                            </div>
                        </div>
                    ))}
            </div>
        </>
    );
}

export default ReportSummaryPage;