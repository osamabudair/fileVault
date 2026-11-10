import { useState, useRef, useEffect } from 'react';

export const Dropdown = ({
  trigger,
  items = [],
  align = 'right',
  width = 'w-44',
  className = ''
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    const handleEscape = (e) => {
      if (e.key === 'Escape') setIsOpen(false);
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      document.addEventListener('keydown', handleEscape);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscape);
    };
  }, [isOpen]);

  const alignmentClasses = {
    left: 'left-0 origin-top-left',
    right: 'right-0 origin-top-right'
  };

  return (
    <div className={`relative inline-block text-left ${className}`} ref={dropdownRef}>
      <div onClick={() => setIsOpen(!isOpen)} className="cursor-pointer">
        {trigger}
      </div>

      {isOpen && (
        <div
          className={`absolute z-30 mt-1 ${width} ${alignmentClasses[align] || alignmentClasses.right} rounded-xl bg-white/95 dark:bg-[#181B20]/95 backdrop-blur-xl border border-black/10 dark:border-white/[0.1] shadow-xl py-1 text-xs divide-y divide-black/[0.04] dark:divide-white/[0.04] focus:outline-none`}
        >
          <div className="py-0.5">
            {items.map((item, index) => {
              if (item.divider) {
                return <div key={`divider-${index}`} className="my-1 border-t border-black/[0.06] dark:border-white/[0.06]" />;
              }

              const Icon = item.icon;
              return (
                <button
                  key={index}
                  type="button"
                  disabled={item.disabled}
                  onClick={(e) => {
                    e.stopPropagation();
                    if (!item.disabled) {
                      setIsOpen(false);
                      item.onClick && item.onClick();
                    }
                  }}
                  className={`w-full flex items-center gap-2 px-3 py-1.5 text-left transition-colors cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed
                    ${item.danger 
                      ? 'text-red-600 dark:text-red-400 hover:bg-red-500/10' 
                      : 'text-zinc-700 dark:text-zinc-300 hover:bg-black/[0.05] dark:hover:bg-white/[0.06] hover:text-zinc-900 dark:hover:text-zinc-100'
                    }`}
                >
                  {Icon && <Icon className="w-3.5 h-3.5 shrink-0 opacity-80" />}
                  <span className="truncate">{item.label}</span>
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default Dropdown;
