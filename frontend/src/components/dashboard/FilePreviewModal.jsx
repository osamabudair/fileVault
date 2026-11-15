import { useState } from 'react';
import { useFiles } from '../../context/FileContext';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { formatBytes, formatDate } from '../../api/mockData';
import { 
  FileText, 
  FileArchive, 
  FileImage, 
  FileCode, 
  File, 
  Download, 
  Share2, 
  Star, 
  Copy, 
  Check, 
  Play, 
  Pause, 
  Volume2, 
  Maximize2,
  FolderOpen
} from 'lucide-react';

function getCategoryIcon(category) {
  switch (category) {
    case 'documents':
      return <FileText className="w-5 h-5 text-[#721C24] dark:text-[#9F2D3B]" />;
    case 'media':
      return <FileImage className="w-5 h-5 text-[#721C24] dark:text-[#9F2D3B]" />;
    case 'archives':
      return <FileArchive className="w-5 h-5 text-[#721C24] dark:text-[#9F2D3B]" />;
    case 'code':
      return <FileCode className="w-5 h-5 text-[#721C24] dark:text-[#9F2D3B]" />;
    default:
      return <File className="w-5 h-5 text-[#721C24] dark:text-[#9F2D3B]" />;
  }
}

export const FilePreviewModal = () => {
  const { 
    previewModalFile, 
    setPreviewModalFile, 
    setShareModalFile, 
    downloadFile, 
    toggleFavorite 
  } = useFiles();

  const [copiedCode, setCopiedCode] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);

  if (!previewModalFile) return null;

  const file = previewModalFile;
  const ext = file.name.split('.').pop().toLowerCase();

  const handleCopyCode = (codeText) => {
    navigator.clipboard?.writeText(codeText);
    setCopiedCode(true);
    setTimeout(() => setCopiedCode(false), 2000);
  };

  const renderContent = () => {
    // 1. Code View
    if (file.category === 'code' || ['sql', 'yaml', 'yml', 'js', 'json', 'ts'].includes(ext)) {
      let sampleCode = '';
      if (ext === 'sql') {
        sampleCode = `-- FileVault Database Schema Migration v2.4
CREATE TABLE IF NOT EXISTS vault_storage_objects (
    id VARCHAR(64) PRIMARY KEY,
    vault_id VARCHAR(64) NOT NULL,
    object_name VARCHAR(255) NOT NULL,
    byte_size BIGINT NOT NULL,
    mime_type VARCHAR(128) NOT NULL,
    checksum_sha256 CHAR(64) NOT NULL,
    is_encrypted BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vault_objects_user ON vault_storage_objects(vault_id);
-- Migration completed successfully`;
      } else if (ext === 'yaml' || ext === 'yml') {
        sampleCode = `apiVersion: apps/v1
kind: Deployment
metadata:
  name: filevault-storage-node
  namespace: production
  labels:
    app.kubernetes.io/name: filevault
    app.kubernetes.io/component: storage-core
spec:
  replicas: 4
  selector:
    matchLabels:
      app: filevault-node
  template:
    metadata:
      labels:
        app: filevault-node
    spec:
      containers:
      - name: storage-worker
        image: filevault.internal/storage-engine:v3.2.1
        resources:
          limits:
            memory: "4Gi"
            cpu: "2000m"`;
      } else {
        sampleCode = `/**
 * FileVault Worker Script
 * Automated cache invalidation and integrity verification
 */
export async function verifyChunkIntegrity(chunkBuffer, expectedHash) {
  const hashBuffer = await crypto.subtle.digest('SHA-256', chunkBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const calculatedHash = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  
  if (calculatedHash !== expectedHash) {
    throw new Error('Integrity check failed: checksum mismatch detected.');
  }
  return true;
}`;
      }

      const lines = sampleCode.split('\n');

      return (
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs text-zinc-500 pb-1 border-b border-black/[0.05] dark:border-white/[0.06]">
            <span className="font-mono text-[11px]">{file.name} • {lines.length} lines</span>
            <Button
              variant="secondary"
              size="sm"
              icon={copiedCode ? Check : Copy}
              onClick={() => handleCopyCode(sampleCode)}
              className="text-[11px] h-7"
            >
              {copiedCode ? 'Copied' : 'Copy Code'}
            </Button>
          </div>

          <div className="rounded-xl bg-zinc-950 p-4 font-mono text-[11px] leading-relaxed text-zinc-300 overflow-x-auto max-h-[380px] border border-zinc-800">
            <table className="w-full">
              <tbody>
                {lines.map((line, idx) => (
                  <tr key={idx} className="hover:bg-zinc-900/50">
                    <td className="text-zinc-600 select-none pr-4 text-right w-8 align-top">
                      {idx + 1}
                    </td>
                    <td className="whitespace-pre text-zinc-200">
                      {line}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      );
    }

    // 2. Images
    if (['png', 'jpg', 'jpeg', 'webp', 'svg', 'gif'].includes(ext)) {
      return (
        <div className="flex flex-col items-center justify-center p-6 rounded-2xl bg-black/[0.02] dark:bg-black/30 border border-black/[0.04] dark:border-white/[0.04] space-y-3">
          <div className="relative max-h-[340px] w-full flex items-center justify-center overflow-hidden rounded-xl bg-zinc-900/5 dark:bg-zinc-900/40 p-4">
            <svg
              className="w-full max-h-[280px] text-zinc-800 dark:text-zinc-200"
              viewBox="0 0 600 320"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <rect width="600" height="320" rx="16" fill="#181B20" fillOpacity="0.08" />
              <rect x="40" y="40" width="140" height="90" rx="10" stroke="currentColor" strokeWidth="2" strokeDasharray="4 4" />
              <text x="60" y="85" fill="currentColor" fontSize="12" fontFamily="sans-serif">Frontend Client</text>
              <text x="75" y="105" fill="#721C24" fontSize="10" fontFamily="sans-serif">React + Vite</text>

              <path d="M180 85 H260" stroke="currentColor" strokeWidth="2" markerEnd="url(#arrow)" />
              
              <rect x="260" y="40" width="140" height="90" rx="10" stroke="#721C24" strokeWidth="2" />
              <text x="280" y="85" fill="currentColor" fontSize="12" fontFamily="sans-serif">API Gateway</text>
              <text x="295" y="105" fill="#721C24" fontSize="10" fontFamily="sans-serif">Spring Boot</text>

              <path d="M400 85 H460" stroke="currentColor" strokeWidth="2" />

              <rect x="460" y="40" width="100" height="90" rx="10" stroke="currentColor" strokeWidth="2" />
              <text x="480" y="85" fill="currentColor" fontSize="12" fontFamily="sans-serif">Cloud Vault</text>
              <text x="490" y="105" fill="#721C24" fontSize="10" fontFamily="sans-serif">Encrypted S3</text>

              <rect x="230" y="190" width="200" height="80" rx="12" stroke="currentColor" strokeWidth="1.5" strokeOpacity="0.5" />
              <text x="255" y="235" fill="currentColor" fontSize="11" fontFamily="sans-serif">Zero-Knowledge Key Ring</text>
            </svg>
          </div>
          <div className="flex items-center justify-between w-full text-[11px] text-zinc-400 font-mono px-2">
            <span>Resolution: 1920 × 1080 px</span>
            <span>MIME: {file.type}</span>
          </div>
        </div>
      );
    }

    // 3. Media / Video
    if (['mp4', 'mov', 'mp3', 'wav'].includes(ext)) {
      return (
        <div className="space-y-3">
          <div className="relative w-full h-64 rounded-2xl bg-zinc-950 flex flex-col items-center justify-center overflow-hidden border border-zinc-800 text-white">
            <button
              onClick={() => setIsPlaying(!isPlaying)}
              className="w-14 h-14 rounded-full bg-[#721C24] dark:bg-[#9F2D3B] hover:scale-105 transition-transform flex items-center justify-center text-white shadow-xl cursor-pointer"
            >
              {isPlaying ? <Pause className="w-6 h-6" /> : <Play className="w-6 h-6 ml-0.5" />}
            </button>

            <div className="absolute bottom-0 inset-x-0 p-3 bg-gradient-to-t from-black/80 to-transparent flex items-center justify-between text-xs font-mono text-zinc-300">
              <div className="flex items-center gap-2">
                <span>{isPlaying ? '01:24' : '00:00'}</span>
                <span>/</span>
                <span>08:45</span>
              </div>
              <div className="flex items-center gap-3">
                <Volume2 className="w-4 h-4 cursor-pointer" />
                <Maximize2 className="w-4 h-4 cursor-pointer" />
              </div>
            </div>
          </div>
          <p className="text-center text-xs text-zinc-400">
            {file.name} • 1080p High Definition Recording
          </p>
        </div>
      );
    }

    // 4. Archives (.zip, .tar.gz)
    if (['zip', 'tar', 'gz', 'tgz', 'rar', '7z'].includes(ext)) {
      const archiveFiles = [
        { name: 'vectors/logo-primary.svg', size: '420 KB', date: 'Nov 4, 2026' },
        { name: 'vectors/logo-monochrome.svg', size: '380 KB', date: 'Nov 4, 2026' },
        { name: 'typography/Inter-Variable.woff2', size: '2.4 MB', date: 'Nov 1, 2026' },
        { name: 'typography/JetBrainsMono-Variable.woff2', size: '1.8 MB', date: 'Nov 1, 2026' },
        { name: 'brand-guidelines-2026.pdf', size: '8.2 MB', date: 'Nov 5, 2026' },
        { name: 'color-palette-tokens.json', size: '14 KB', date: 'Nov 6, 2026' }
      ];

      return (
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs text-zinc-500 pb-1 border-b border-black/[0.05] dark:border-white/[0.06]">
            <span className="flex items-center gap-1.5 font-medium">
              <FolderOpen className="w-3.5 h-3.5" />
              <span>Archive Manifest ({archiveFiles.length} files)</span>
            </span>
            <span className="font-mono text-[11px]">Uncompressed: ~14 MB</span>
          </div>

          <div className="rounded-xl border border-black/[0.05] dark:border-white/[0.06] overflow-hidden max-h-64 overflow-y-auto">
            <table className="w-full text-left text-xs">
              <tbody className="divide-y divide-black/[0.03] dark:divide-white/[0.04]">
                {archiveFiles.map((item, i) => (
                  <tr key={i} className="hover:bg-black/[0.02] dark:hover:bg-white/[0.02]">
                    <td className="py-2 px-3 font-mono text-[11px] text-zinc-800 dark:text-zinc-200">
                      {item.name}
                    </td>
                    <td className="py-2 px-3 text-right font-mono text-[11px] text-zinc-400">
                      {item.size}
                    </td>
                    <td className="py-2 px-3 text-right text-[11px] text-zinc-400">
                      {item.date}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      );
    }

    // 5. Default / Documents (.pdf, .docx, .txt, etc.)
    return (
      <div className="space-y-4">
        <div className="p-6 rounded-2xl bg-white/70 dark:bg-black/30 border border-black/[0.05] dark:border-white/[0.06] space-y-4">
          <div className="border-b border-black/[0.05] dark:border-white/[0.06] pb-3">
            <span className="text-[10px] font-mono uppercase tracking-wider text-[#721C24] dark:text-[#9F2D3B] font-semibold">
              Confidential Document
            </span>
            <h3 className="text-base font-semibold text-zinc-900 dark:text-zinc-100 mt-1">
              {file.name.replace(/\.[^/.]+$/, '').replace(/-/g, ' ').toUpperCase()}
            </h3>
            <p className="text-xs text-zinc-400 mt-0.5">
              Prepared for FileVault Executive & Infrastructure Review
            </p>
          </div>

          <div className="space-y-2 text-xs text-zinc-600 dark:text-zinc-300 leading-relaxed font-sans">
            <p className="font-medium text-zinc-800 dark:text-zinc-200">
              1. Executive Summary & Infrastructure Overview
            </p>
            <p>
              This document contains the verified operational benchmarks, financial attestation, and data governance standards implemented for the private cloud repository. All stored assets conform to the strict zero-knowledge architecture.
            </p>
            <div className="grid grid-cols-2 gap-3 pt-2">
              <div className="p-3 rounded-xl bg-black/[0.02] dark:bg-white/[0.02] border border-black/[0.04] dark:border-white/[0.04]">
                <p className="text-[10px] text-zinc-400 uppercase tracking-wider">Storage Tier</p>
                <p className="text-sm font-semibold text-zinc-800 dark:text-zinc-200 mt-0.5">Enterprise 50 GB</p>
              </div>
              <div className="p-3 rounded-xl bg-black/[0.02] dark:bg-white/[0.02] border border-black/[0.04] dark:border-white/[0.04]">
                <p className="text-[10px] text-zinc-400 uppercase tracking-wider">Access Status</p>
                <p className="text-sm font-semibold text-[#721C24] dark:text-[#9F2D3B] mt-0.5">
                  {file.isShared ? 'Shared Access Active' : 'Private & Restricted'}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <Modal
      isOpen={!!previewModalFile}
      onClose={() => setPreviewModalFile(null)}
      title={
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-xl bg-black/[0.04] dark:bg-white/[0.06] flex items-center justify-center shrink-0">
            {getCategoryIcon(file.category)}
          </div>
          <div>
            <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100 truncate max-w-sm">
              {file.name}
            </h3>
            <p className="text-[11px] text-zinc-400 font-mono">
              {formatBytes(file.size)} • {formatDate(file.updatedAt)}
            </p>
          </div>
        </div>
      }
      maxWidth="max-w-2xl"
      footer={
        <div className="flex items-center justify-between w-full">
          <div className="flex items-center gap-2">
            <button
              onClick={() => toggleFavorite(file.id)}
              className={`p-1.5 rounded-lg border border-black/[0.06] dark:border-white/[0.08] transition-colors cursor-pointer ${
                file.isFavorite 
                  ? 'text-[#721C24] dark:text-[#9F2D3B] bg-[#721C24]/10 dark:bg-[#9F2D3B]/10' 
                  : 'text-zinc-400 hover:text-[#721C24] dark:hover:text-[#9F2D3B]'
              }`}
              title={file.isFavorite ? 'Remove Favorite' : 'Mark as Favorite'}
            >
              <Star className="w-4 h-4 fill-current" />
            </button>
            <Button
              variant="secondary"
              size="sm"
              icon={Share2}
              onClick={() => {
                setPreviewModalFile(null);
                setShareModalFile(file);
              }}
            >
              Share Link
            </Button>
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="primary"
              size="sm"
              icon={Download}
              onClick={() => downloadFile(file)}
            >
              Download
            </Button>
          </div>
        </div>
      }
    >
      <div className="py-2">
        {renderContent()}
      </div>
    </Modal>
  );
};

export default FilePreviewModal;
