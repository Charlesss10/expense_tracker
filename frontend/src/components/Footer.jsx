import React from 'react';
import { FaLinkedin } from 'react-icons/fa';

function Footer() {
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-section">
          <h4>Expense Tracker</h4>
          <p>Manage your finances with ease. Track expenses, monitor balances, and generate insightful reports.</p>
        </div>
        <div className="footer-section">
          <h4>Quick Links</h4>
          <ul>
            <li><a href="/balance">Dashboard</a></li>
            <li><a href="/transactions">Transactions</a></li>
            <li><a href="/report-summary">Reports</a></li>
            <li><a href="/settings">Settings</a></li>
          </ul>
        </div>
        <div className="footer-section">
          <h4>Contact</h4>
          <p>
            <a href="mailto:ebosoncharles8@gmail.com" className="contact-link">
              Email: ebosoncharles8@gmail.com
            </a>
          </p>
          <p>
            <a href="https://linkedin.com/in/charlesboson" target="_blank" rel="noopener noreferrer" className="contact-link linkedin-link">
              <FaLinkedin className="linkedin-icon" /> https://www.linkedin.com/in/charles-eboson
            </a>
          </p>
        </div>
      </div>
      <div className="footer-bottom">
        <p>&copy; 2026 CharlesTech Solutions. All rights reserved.</p>
      </div>
    </footer>
  );
}

export default Footer;