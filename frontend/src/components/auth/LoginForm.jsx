import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Input } from '../common/Input';
import { Button } from '../common/Button';
import { Mail, Lock, Eye, EyeOff } from 'lucide-react';

export const LoginForm = ({ onSwitchMode }) => {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    if (!email.trim()) {
      setError('Email address is required.');
      return;
    }
    if (!password) {
      setError('Password is required.');
      return;
    }

    setLoading(true);
    try {
      login(email, password);
    } catch (err) {
      setError(err.message || 'Authentication failed.');
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input
        label="Email Address"
        type="email"
        placeholder="example@gmail.com"
        icon={Mail}
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        autoComplete="email"
        required
      />

      <Input
        label="Password"
        type={showPassword ? 'text' : 'password'}
        placeholder="••••••••••••"
        icon={Lock}
        trailingIcon={showPassword ? EyeOff : Eye}
        onTrailingClick={() => setShowPassword(!showPassword)}
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        autoComplete="current-password"
        required
      />

      {error && (
        <div className="p-2.5 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs">
          {error}
        </div>
      )}

      <div className="pt-1">
        <Button
          type="submit"
          variant="primary"
          size="lg"
          loading={loading}
          className="w-full text-xs text-center justify-center"
        >
          Sign In
        </Button>
      </div>

      {/* Switch to Sign Up */}
      <div className="pt-2 text-center">
        <p className="text-xs text-zinc-500 dark:text-zinc-400">
          Don&apos;t have an account?{' '}
          <button
            type="button"
            onClick={onSwitchMode}
            className="font-medium text-[#721C24] dark:text-[#9F2D3B] hover:underline cursor-pointer ml-1 inline-block"
          >
            Sign Up
          </button>
        </p>
      </div>
    </form>
  );
};

export default LoginForm;
