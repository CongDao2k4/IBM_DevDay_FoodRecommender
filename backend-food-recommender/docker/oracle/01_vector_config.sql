-- =====================================================
-- Oracle 23ai Vector Memory Configuration
-- Description: Configure vector memory pool to prevent ORA-51962
-- =====================================================

-- Increase vector memory pool size for the PDB
-- Default is often too small for multiple vector indexes
ALTER SYSTEM SET VECTOR_MEMORY_SIZE = 512M SCOPE=BOTH;

-- Enable vector memory auto-tuning (if available)
-- This allows Oracle to dynamically adjust vector memory
ALTER SYSTEM SET VECTOR_MEMORY_AUTO_TUNE = TRUE SCOPE=BOTH;

-- Verify settings
SELECT name, value, description 
FROM v$parameter 
WHERE name LIKE '%vector%';

-- Made with Bob