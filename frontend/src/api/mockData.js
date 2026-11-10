/**
 * Clean mock dataset for FileVault
 * Compatible with Spring Boot FileDTO
 */

export const INITIAL_USER = {
  id: "usr_94812",
  name: "Alexander Vance",
  email: "alexander.vance@filevault.io",
  avatarInitials: "AV",
  storageUsed: 15247118336, // 14.2 GB
  storageTotal: 53687091200 // 50 GB
};

export const INITIAL_FILES = [
  {
    id: "fl_01",
    name: "quarterly-financial-review-2026.pdf",
    size: 4718592, // 4.5 MB
    type: "application/pdf",
    category: "documents",
    updatedAt: "2026-11-08T14:20:00Z",
    isShared: true,
    downloadUrl: "/api/files/fl_01/download",
    isFavorite: true,
    isTrash: false,
    sharedWith: ["Elena Rostova", "David Chen"]
  },
  {
    id: "fl_02",
    name: "product-roadmap-specifications.docx",
    size: 1843200, // 1.75 MB
    type: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    category: "documents",
    updatedAt: "2026-11-07T11:45:00Z",
    isShared: false,
    downloadUrl: "/api/files/fl_02/download",
    isFavorite: false,
    isTrash: false,
    sharedWith: []
  },
  {
    id: "fl_03",
    name: "brand-assets-vector-pack.zip",
    size: 14680064, // 14 MB
    type: "application/zip",
    category: "archives",
    updatedAt: "2026-11-06T16:10:00Z",
    isShared: true,
    downloadUrl: "/api/files/fl_03/download",
    isFavorite: true,
    isTrash: false,
    sharedWith: ["Design Team"]
  },
  {
    id: "fl_04",
    name: "executive-briefing-record.mp4",
    size: 89128960, // 85 MB
    type: "video/mp4",
    category: "media",
    updatedAt: "2026-11-05T09:30:00Z",
    isShared: false,
    downloadUrl: "/api/files/fl_04/download",
    isFavorite: false,
    isTrash: false,
    sharedWith: []
  },
  {
    id: "fl_05",
    name: "system-architecture-v3.png",
    size: 5242880, // 5 MB
    type: "image/png",
    category: "media",
    updatedAt: "2026-11-04T18:15:00Z",
    isShared: true,
    downloadUrl: "/api/files/fl_05/download",
    isFavorite: false,
    isTrash: false,
    sharedWith: ["DevOps Core"]
  },
  {
    id: "fl_06",
    name: "cloud-cluster-config.yaml",
    size: 28672, // 28 KB
    type: "text/yaml",
    category: "code",
    updatedAt: "2026-11-03T13:00:00Z",
    isShared: false,
    downloadUrl: "/api/files/fl_06/download",
    isFavorite: true,
    isTrash: false,
    sharedWith: []
  },
  {
    id: "fl_07",
    name: "database-schema-migration.sql",
    size: 163840, // 160 KB
    type: "text/x-sql",
    category: "code",
    updatedAt: "2026-11-02T10:22:00Z",
    isShared: false,
    downloadUrl: "/api/files/fl_07/download",
    isFavorite: false,
    isTrash: false,
    sharedWith: []
  },
  {
    id: "fl_08",
    name: "annual-shareholder-summary.pdf",
    size: 7340032, // 7 MB
    type: "application/pdf",
    category: "documents",
    updatedAt: "2026-10-29T15:00:00Z",
    isShared: true,
    downloadUrl: "/api/files/fl_08/download",
    isFavorite: false,
    isTrash: false,
    sharedWith: ["Board Members"]
  },
  {
    id: "fl_09",
    name: "deprecated-cache-worker.js",
    size: 12288, // 12 KB
    type: "application/javascript",
    category: "code",
    updatedAt: "2026-10-18T08:12:00Z",
    isShared: false,
    downloadUrl: "/api/files/fl_09/download",
    isFavorite: false,
    isTrash: true,
    sharedWith: []
  }
];

export function formatBytes(bytes, decimals = 1) {
  if (!bytes || bytes === 0) return "0 B";
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
}

export function formatDate(dateString) {
  if (!dateString) return "—";
  const date = new Date(dateString);
  const now = new Date();
  const diffHours = Math.floor((now - date) / (1000 * 60 * 60));
  const diffDays = Math.floor(diffHours / 24);

  if (diffHours < 1) return "Just now";
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays === 1) return "Yesterday";
  if (diffDays < 7) return `${diffDays}d ago`;

  return date.toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: date.getFullYear() !== now.getFullYear() ? "numeric" : undefined
  });
}

export function getFileCategory(fileName, mimeType = "") {
  const ext = fileName.split(".").pop().toLowerCase();
  if (["pdf", "doc", "docx", "txt", "rtf", "odt", "csv", "xlsx", "pptx"].includes(ext) || mimeType.includes("pdf") || mimeType.includes("document")) {
    return "documents";
  }
  if (["png", "jpg", "jpeg", "webp", "svg", "gif", "mp4", "mov", "mp3", "wav"].includes(ext) || mimeType.startsWith("image/") || mimeType.startsWith("video/") || mimeType.startsWith("audio/")) {
    return "media";
  }
  if (["zip", "tar", "gz", "tgz", "rar", "7z"].includes(ext) || mimeType.includes("zip") || mimeType.includes("tar") || mimeType.includes("archive")) {
    return "archives";
  }
  if (["js", "jsx", "ts", "tsx", "json", "yaml", "yml", "sql", "html", "css", "py", "java", "go", "rs", "sh"].includes(ext)) {
    return "code";
  }
  return "documents";
}
