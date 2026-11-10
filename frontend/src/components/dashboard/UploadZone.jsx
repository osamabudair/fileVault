import { useRef, useState } from 'react';
import { useFiles } from '../../context/FileContext';
import { UploadCloud } from 'lucide-react';

export const UploadZone = () => {
  const { addFiles } = useFiles();
  const [isDragOver, setIsDragOver] = useState(false);
  const fileInputRef = useRef(null);

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      addFiles(e.dataTransfer.files);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      addFiles(e.target.files);
      e.target.value = '';
    }
  };

  return (
    <div>
      <input
        ref={fileInputRef}
        type="file"
        multiple
        onChange={handleFileChange}
        className="hidden"
      />

      <div
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={() => fileInputRef.current?.click()}
        className={`rounded-2xl border-dashed border transition-all duration-150 p-6 text-center cursor-pointer select-none ${
          isDragOver
            ? 'border-[#721C24] dark:border-[#9F2D3B] bg-[#721C24]/[0.03] dark:bg-[#9F2D3B]/[0.05]'
            : 'border-zinc-300 dark:border-zinc-700/80 bg-white/40 dark:bg-white/[0.02] hover:bg-white/70 dark:hover:bg-white/[0.04]'
        }`}
      >
        <div className="flex flex-col items-center justify-center gap-2 max-w-sm mx-auto">
          <div className="w-10 h-10 rounded-xl bg-black/[0.04] dark:bg-white/[0.06] flex items-center justify-center text-zinc-600 dark:text-zinc-300">
            <UploadCloud className="w-5 h-5 text-[#721C24] dark:text-[#9F2D3B]" />
          </div>
          <div>
            <p className="text-xs font-medium text-zinc-800 dark:text-zinc-200">
              <span className="text-[#721C24] dark:text-[#9F2D3B] underline underline-offset-2">Click to upload</span> or drag and drop files
            </p>
            <p className="text-[11px] text-zinc-400 dark:text-zinc-500 mt-0.5">
              Supports documents, media, archives, and code files
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UploadZone;
