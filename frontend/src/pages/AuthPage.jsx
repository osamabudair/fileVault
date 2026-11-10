import { useState } from 'react';
import { GlassCard } from '../components/common/GlassCard';
import { LoginForm } from '../components/auth/LoginForm';
import { RegisterForm } from '../components/auth/RegisterForm';
import { ThemeToggle } from '../components/common/ThemeToggle';
import { Vault } from 'lucide-react';

export const AuthPage = () => {
  const [mode, setMode] = useState('login'); // 'login' | 'register'

  return (
    <div className="min-h-screen w-full bg-[#EBECEF] dark:bg-[#0F1115] flex flex-col items-center justify-center p-4 relative selection:bg-[#721C24]/20 selection:text-[#721C24] dark:selection:bg-[#9F2D3B]/30 dark:selection:text-[#f3a8b2] transition-colors duration-150 font-sans">
      {/* Top minimal control */}
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
        <GlassCard className="p-6">
          {/* Mode Switcher Tabs */}
          <div className="flex rounded-xl bg-black/[0.04] dark:bg-white/[0.04] p-1 mb-5 border border-black/[0.04] dark:border-white/[0.04]">
            <button
              type="button"
              onClick={() => setMode('login')}
              className={`flex-1 py-1.5 text-xs font-medium rounded-lg transition-all duration-150 cursor-pointer ${
                mode === 'login'
                  ? 'bg-white dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 shadow-xs'
                  : 'text-zinc-500 dark:text-zinc-400 hover:text-zinc-800 dark:hover:text-zinc-200'
              }`}
            >
              Sign In
            </button>
            <button
              type="button"
              onClick={() => setMode('register')}
              className={`flex-1 py-1.5 text-xs font-medium rounded-lg transition-all duration-150 cursor-pointer ${
                mode === 'register'
                  ? 'bg-white dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 shadow-xs'
                  : 'text-zinc-500 dark:text-zinc-400 hover:text-zinc-800 dark:hover:text-zinc-200'
              }`}
            >
              Sign Up
            </button>
          </div>

          {/* Form */}
          {mode === 'login' ? <LoginForm /> : <RegisterForm />}
        </GlassCard>
      </div>
    </div>
  );
};

export default AuthPage;
