import { forwardRef } from 'react';
import { Loader2 } from 'lucide-react';

export const Button = forwardRef(({
  children,
  variant = 'primary',
  size = 'md',
  icon: Icon,
  iconPosition = 'left',
  loading = false,
  disabled = false,
  className = '',
  type = 'button',
  ...props
}, ref) => {
  const baseStyles = 'inline-flex items-center justify-center font-medium rounded-xl select-none focus:outline-none focus-visible:ring-2 focus-visible:ring-[#721C24]/30 dark:focus-visible:ring-[#9F2D3B]/40 disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer whitespace-nowrap active:scale-[0.99] transition-colors duration-150';

  const variants = {
    // Executive Burgundy / Wine primary accent
    primary: 'bg-[#721C24] hover:bg-[#5C161D] dark:bg-[#9F2D3B] dark:hover:bg-[#862431] text-white shadow-xs',
    // Refined frosted secondary
    secondary: 'bg-black/[0.04] hover:bg-black/[0.08] dark:bg-white/[0.06] dark:hover:bg-white/[0.10] text-zinc-800 dark:text-zinc-200 border border-black/[0.06] dark:border-white/[0.08]',
    // Translucent glass button
    glass: 'bg-white/60 hover:bg-white/90 dark:bg-zinc-800/60 dark:hover:bg-zinc-800/90 text-zinc-800 dark:text-zinc-200 border border-white/60 dark:border-white/[0.08] backdrop-blur-sm',
    // Ghost
    ghost: 'bg-transparent hover:bg-black/[0.04] dark:hover:bg-white/[0.06] text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100',
    // Danger
    danger: 'bg-red-500/10 hover:bg-red-500/15 text-red-700 dark:text-red-400 border border-red-500/20'
  };

  const sizes = {
    sm: 'h-8 text-xs px-3 gap-1.5',
    md: 'h-9 text-xs px-3.5 gap-2',
    lg: 'h-10 text-sm px-4.5 gap-2.5'
  };

  return (
    <button
      ref={ref}
      type={type}
      disabled={disabled || loading}
      className={`${baseStyles} ${variants[variant] || variants.primary} ${sizes[size] || sizes.md} ${className}`}
      {...props}
    >
      {loading ? (
        <Loader2 className="w-3.5 h-3.5 animate-spin" />
      ) : (
        Icon && iconPosition === 'left' && <Icon className="w-3.5 h-3.5 shrink-0" />
      )}
      <span>{children}</span>
      {!loading && Icon && iconPosition === 'right' && (
        <Icon className="w-3.5 h-3.5 shrink-0" />
      )}
    </button>
  );
});

Button.displayName = 'Button';
export default Button;
