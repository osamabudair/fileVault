export const GlassCard = ({
  children,
  className = '',
  hoverEffect = false,
  ...props
}) => {
  return (
    <div
      className={`rounded-2xl bg-white/85 dark:bg-[#181B20]/85 border border-white/80 dark:border-white/[0.08] shadow-[0_4px_20px_rgba(0,0,0,0.03)] dark:shadow-[0_8px_32px_rgba(0,0,0,0.37)] transition-colors duration-100 ${
        hoverEffect ? 'hover:bg-white/95 dark:hover:bg-[#181B20]/95' : ''
      } ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

export default GlassCard;
