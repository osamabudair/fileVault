import { forwardRef } from 'react';

export const Input = forwardRef(({
  label,
  error,
  helperText,
  icon: Icon,
  trailingIcon: TrailingIcon,
  onTrailingClick,
  className = '',
  type = 'text',
  ...props
}, ref) => {
  return (
    <div className="w-full space-y-1.5">
      {label && (
        <label className="block text-xs font-medium text-zinc-700 dark:text-zinc-300">
          {label}
        </label>
      )}

      <div className="relative flex items-center">
        {Icon && (
          <div className="absolute left-3 flex items-center pointer-events-none text-zinc-400 dark:text-zinc-500">
            <Icon className="w-4 h-4" />
          </div>
        )}

        <input
          ref={ref}
          type={type}
          className={`w-full h-9 rounded-xl bg-white/60 focus:bg-white/95 dark:bg-white/[0.04] dark:focus:bg-[#181B20] border text-xs text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 backdrop-blur-xs transition-colors duration-150 outline-none
            ${Icon ? 'pl-9' : 'pl-3.5'}
            ${TrailingIcon ? 'pr-9' : 'pr-3.5'}
            ${error
              ? 'border-red-500/60 focus:border-red-500 ring-1 ring-red-500/20'
              : 'border-black/10 dark:border-white/[0.1] focus:border-[#721C24]/50 dark:focus:border-[#9F2D3B]/60 focus:ring-1 focus:ring-[#721C24]/20 dark:focus:ring-[#9F2D3B]/30'
            }
            disabled:opacity-50 disabled:cursor-not-allowed
            ${className}`}
          {...props}
        />

        {TrailingIcon && (
          <button
            type="button"
            onClick={onTrailingClick}
            className="absolute right-3 flex items-center text-zinc-400 hover:text-zinc-700 dark:text-zinc-500 dark:hover:text-zinc-300 cursor-pointer transition-colors"
          >
            <TrailingIcon className="w-4 h-4" />
          </button>
        )}
      </div>

      {error ? (
        <p className="text-[11px] text-red-600 dark:text-red-400 font-medium pl-0.5">{error}</p>
      ) : helperText ? (
        <p className="text-[11px] text-zinc-500 pl-0.5">{helperText}</p>
      ) : null}
    </div>
  );
});

Input.displayName = 'Input';
export default Input;
