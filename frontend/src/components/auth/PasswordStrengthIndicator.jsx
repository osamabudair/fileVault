import { useMemo } from 'react';
import { Check, X } from 'lucide-react';

export const PasswordStrengthIndicator = ({ password = '' }) => {
  const analysis = useMemo(() => {
    if (!password) {
      return { score: 0, label: '', checks: [] };
    }

    const checks = [
      { id: 'length', label: '8+ chars', passed: password.length >= 8 },
      { id: 'uppercase', label: 'A-Z', passed: /[A-Z]/.test(password) },
      { id: 'number', label: '0-9', passed: /[0-9]/.test(password) },
      { id: 'special', label: 'Symbol', passed: /[^A-Za-z0-9]/.test(password) }
    ];

    const passedCount = checks.filter(c => c.passed).length;

    let score = 0;
    let label = 'Very Weak';

    if (password.length > 0) {
      if (passedCount <= 1) {
        score = 1;
        label = 'Weak';
      } else if (passedCount === 2) {
        score = 2;
        label = 'Fair';
      } else if (passedCount === 3) {
        score = 3;
        label = 'Good';
      } else if (passedCount === 4) {
        score = 4;
        label = 'Strong';
      }
    }

    return { score, label, checks };
  }, [password]);

  const isVisible = password.length > 0;

  const getBarColor = (index) => {
    if (index >= analysis.score) {
      return 'bg-black/[0.06] dark:bg-white/[0.08]';
    }
    switch (analysis.score) {
      case 1:
        return 'bg-rose-500';
      case 2:
        return 'bg-amber-500';
      case 3:
        return 'bg-sky-500';
      case 4:
        return 'bg-emerald-500';
      default:
        return 'bg-black/[0.06] dark:bg-white/[0.08]';
    }
  };

  const getLabelColor = () => {
    switch (analysis.score) {
      case 1:
        return 'text-rose-500';
      case 2:
        return 'text-amber-500';
      case 3:
        return 'text-sky-500';
      case 4:
        return 'text-emerald-500';
      default:
        return 'text-zinc-400';
    }
  };

  return (
    <div
      className={`grid transition-all duration-300 ease-[cubic-bezier(0.16,1,0.3,1)] overflow-hidden ${
        isVisible
          ? 'grid-rows-[1fr] opacity-100 max-h-28 mt-2'
          : 'grid-rows-[0fr] opacity-0 max-h-0 mt-0 pointer-events-none'
      }`}
    >
      <div className="overflow-hidden space-y-2">
        {/* Top row: Bar + Label */}
        <div className="space-y-1">
          <div className="flex items-center justify-between text-[11px]">
            <span className="text-zinc-500 dark:text-zinc-400 font-medium">Password Strength</span>
            <span className={`font-semibold transition-colors duration-200 ${getLabelColor()}`}>
              {analysis.label}
            </span>
          </div>

          {/* 4-segment progress bar */}
          <div className="grid grid-cols-4 gap-1.5 h-1.5 w-full">
            {[0, 1, 2, 3].map((index) => (
              <div
                key={index}
                className={`h-full rounded-full transition-all duration-300 ease-out ${getBarColor(index)}`}
              />
            ))}
          </div>
        </div>

        {/* Requirement tags */}
        <div className="flex flex-wrap gap-1.5 pt-0.5">
          {analysis.checks.map((check) => (
            <span
              key={check.id}
              className={`inline-flex items-center gap-1 text-[10px] px-2 py-0.5 rounded-md transition-all duration-200 ${
                check.passed
                  ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 shadow-2xs'
                  : 'bg-black/[0.03] dark:bg-white/[0.03] text-zinc-400 dark:text-zinc-500 border border-black/[0.04] dark:border-white/[0.05]'
              }`}
            >
              {check.passed ? (
                <Check className="w-2.5 h-2.5 shrink-0" />
              ) : (
                <X className="w-2.5 h-2.5 shrink-0 opacity-50" />
              )}
              <span>{check.label}</span>
            </span>
          ))}
        </div>
      </div>
    </div>
  );
};

export default PasswordStrengthIndicator;
