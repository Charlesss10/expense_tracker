function BootingUpScreen({ timeLeft }) {
  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100%',
      height: '100%',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 9999,
      color: 'white',
      textAlign: 'center',
    }}>
      <div>
        <h2>Render Service is Booting Up</h2>
        <p>Our backend service has spun down due to inactivity and is starting up again.</p>
        <p>This usually takes about 2 minutes. Please be patient!</p>
        <div style={{ fontSize: '2rem', marginTop: '20px' }}>
          Time remaining: {minutes}:{seconds.toString().padStart(2, '0')}
        </div>
        <p style={{ marginTop: '20px', fontSize: '0.9rem' }}>
          The app will automatically refresh once the service is ready.
        </p>
      </div>
    </div>
  );
}

export default BootingUpScreen;