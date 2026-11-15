import { useState, useRef, useEffect } from 'react';
import { GlassCard } from '../components/common/GlassCard';
import { LoginForm } from '../components/auth/LoginForm';
import { RegisterForm } from '../components/auth/RegisterForm';
import { ThemeToggle } from '../components/common/ThemeToggle';
import { Vault } from 'lucide-react';

export const AuthPage = () => {
  const [mode, setMode] = useState('login'); // 'login' | 'register'
  const [formHeight, setFormHeight] = useState(null);
  const loginRef = useRef(null);
  const registerRef = useRef(null);

  useEffect(() => {
    const currentEl = mode === 'login' ? loginRef.current : registerRef.current;
    if (!currentEl) return;

    // Set initial height
    setFormHeight(currentEl.offsetHeight);

    // Watch for dynamic height changes (e.g. password strength bar expanding)
    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        setFormHeight(entry.target.offsetHeight);
      }
    });

    observer.observe(currentEl);
    return () => observer.disconnect();
  }, [mode]);

  const handleSwitchToRegister = () => {
    setMode('register');
  };

  const handleSwitchToLogin = () => {
    setMode('login');
  };

  return (
    <div className="min-h-screen w-full bg-[#EBECEF] dark:bg-[#0F1115] flex flex-col items-center justify-center p-4 relative selection:bg-[#721C24]/20 selection:text-[#721C24] dark:selection:bg-[#9F2D3B]/30 dark:selection:text-[#f3a8b2] font-sans">
      {/* Top minimal theme toggle */}
      <div className="absolute top-5 right-5 z-10">
        <ThemeToggle />
      </div>

      <div className="w-full max-w-sm space-y-5">
        {/* Brand Header */}
        <div className="text-center space-y-1.5">
          <div className="inline-flex items-center justify-center w-11 h-11 rounded-2xl bg-white/80 dark:bg-white/[0.06] border border-white/60 dark:border-white/[0.08] shadow-[0_4px_16px_rgba(0,0,0,0.04)] text-zinc-800 dark:text-zinc-200 mb-1">
            <Vault className="w-5 h-5 text-[#721C24] dark:text-[#9F2D3B]" />
          </div>
          <h1 className="text-lg font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">
            FileVault
          </h1>
          <p className="text-xs text-zinc-500 dark:text-zinc-400">
            Private cloud file storage & management
          </p>
        </div>

        {/* Floating Frosted Glass Card */}
        <GlassCard className="p-6 overflow-hidden">
          {/* Mode Switcher Tabs with smooth animated sliding pill */}
          <div className="relative flex rounded-xl bg-black/[0.05] dark:bg-white/[0.05] p-1 mb-6 border border-black/[0.04] dark:border-white/[0.05]">
            {/* Sliding Pill Indicator */}
            <div
              className="absolute top-1 bottom-1 w-[calc(50%-4px)] rounded-lg bg-white dark:bg-[#181B20] border border-black/[0.06] dark:border-white/[0.1] shadow-sm transition-all duration-300 ease-[cubic-bezier(0.16,1,0.3,1)] pointer-events-none"
              style={{
                left: mode === 'register' ? 'calc(50% + 0px)' : '4px'
              }}
            />

            <button
              type="button"
              onClick={handleSwitchToLogin}
              className={`relative z-10 flex-1 py-2 text-xs font-medium rounded-lg transition-colors duration-200 cursor-pointer ${
                mode === 'login'
                  ? 'text-zinc-900 dark:text-zinc-100 font-semibold'
                  : 'text-zinc-500 dark:text-zinc-400 hover:text-zinc-800 dark:hover:text-zinc-200'
              }`}
            >
              Sign In
            </button>
            <button
              type="button"
              onClick={handleSwitchToRegister}
              className={`relative z-10 flex-1 py-2 text-xs font-medium rounded-lg transition-colors duration-200 cursor-pointer ${
                mode === 'register'
                  ? 'text-zinc-900 dark:text-zinc-100 font-semibold'
                  : 'text-zinc-500 dark:text-zinc-400 hover:text-zinc-800 dark:hover:text-zinc-200'
              }`}
            >
              Sign Up
            </button>
          </div>

          {/* Smooth Auto-Height & Horizontal Sliding Carousel */}
          <div
            style={{ height: formHeight ? `${formHeight}px` : 'auto' }}
            className="transition-[height] duration-350 ease-[cubic-bezier(0.16,1,0.3,1)] overflow-hidden"
          >
            <div
              className="flex w-[200%] transition-transform duration-350 ease-[cubic-bezier(0.16,1,0.3,1)] items-start"
              style={{
                transform: mode === 'register' ? 'translateX(-50%)' : 'translateX(0%)'
              }}
            >
              {/* Slide 1: Sign In Form */}
              <div
                ref={loginRef}
                className={`w-1/2 pr-2 transition-opacity duration-250 ${
                  mode === 'login' ? 'opacity-100' : 'opacity-0 pointer-events-none'
                }`}
              >
                <LoginForm onSwitchMode={handleSwitchToRegister} />
              </div>

              {/* Slide 2: Sign Up Form */}
              <div
                ref={registerRef}
                className={`w-1/2 pl-2 transition-opacity duration-250 ${
                  mode === 'register' ? 'opacity-100' : 'opacity-0 pointer-events-none'
                }`}
              >
                <RegisterForm onSwitchMode={handleSwitchToLogin} />
              </div>
            </div>
          </div>
        </GlassCard>
      </div>
    </div>
  );
};

export default AuthPage;
