export const GlassCard = ({
  children,
  className = '',
  hoverEffect = false,
  ...props
}) => {
  return (
    <div
      className={`rounded-2xl bg-white/75 dark:bg-[#181B20]/70 backdrop-blur-md border border-white/60 dark:border-white/[0.08] shadow-[0_4px_20px_rgba(0,0,0,0.04)] dark:shadow-[0_8px_32px_rgba(0,0,0,0.37)] transition-colors duration-150 ${
        hoverEffect ? 'hover:bg-white/85 dark:hover:bg-[#181B20]/85' : ''
      } ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

export default GlassCard;
