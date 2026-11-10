export const Badge = ({
  children,
  variant = 'default',
  size = 'sm',
  dot = false,
  mono = false,
  className = ''
}) => {
  const variants = {
    default: 'bg-zinc-800/80 text-zinc-300 border-zinc-700/60',
    neutral: 'bg-zinc-900 text-zinc-400 border-zinc-800',
    success: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    warning: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
    danger: 'bg-red-500/10 text-red-400 border-red-500/20',
    info: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20'
  };

  const dotColors = {
    default: 'bg-zinc-400',
    neutral: 'bg-zinc-500',
    success: 'bg-emerald-400',
    warning: 'bg-amber-400',
    danger: 'bg-red-400',
    info: 'bg-indigo-400'
  };

  const sizes = {
    xs: 'text-[10px] px-1.5 py-0.5 gap-1',
    sm: 'text-[11px] px-2 py-0.5 gap-1.5',
    md: 'text-xs px-2.5 py-1 gap-1.5'
  };

  return (
    <span
      className={`inline-flex items-center font-medium border rounded select-none ${variants[variant] || variants.default} ${sizes[size] || sizes.sm} ${mono ? 'font-mono text-[10.5px] tabular-nums' : ''} ${className}`}
    >
      {dot && (
        <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${dotColors[variant] || dotColors.default}`} />
      )}
      {children}
    </span>
  );
};

export default Badge;
