-- V1__Initial_Schema.sql

-- Enable UUID and other useful extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "btree_gist";

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Projects table
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    path VARCHAR(500) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_project_path UNIQUE (path)
);

CREATE TRIGGER update_project_updated_at
    BEFORE UPDATE ON projects
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Coding sessions table
CREATE TABLE coding_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    language VARCHAR(100) NOT NULL,
    ide VARCHAR(100) NOT NULL,
    file_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_coding_sessions_project_id ON coding_sessions(project_id);
CREATE INDEX idx_coding_sessions_start_time ON coding_sessions(start_time);
CREATE INDEX idx_coding_sessions_language ON coding_sessions(language);

CREATE TRIGGER update_coding_session_updated_at
    BEFORE UPDATE ON coding_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Git activities table
CREATE TABLE git_activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    coding_session_id UUID NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    commit_hash VARCHAR(40),
    branch_name VARCHAR(255),
    commit_message TEXT,
    files_changed INTEGER,
    insertions INTEGER,
    deletions INTEGER,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_coding_session
        FOREIGN KEY (coding_session_id)
        REFERENCES coding_sessions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_git_activities_session ON git_activities(coding_session_id);
CREATE INDEX idx_git_activities_timestamp ON git_activities(timestamp);
CREATE INDEX idx_git_activities_commit_hash ON git_activities(commit_hash);

CREATE TRIGGER update_git_activity_updated_at
    BEFORE UPDATE ON git_activities
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- IDE events table
CREATE TABLE ide_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    coding_session_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500),
    event_data JSONB,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_coding_session
        FOREIGN KEY (coding_session_id)
        REFERENCES coding_sessions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_ide_events_session ON ide_events(coding_session_id);
CREATE INDEX idx_ide_events_timestamp ON ide_events(timestamp);
CREATE INDEX idx_ide_events_type ON ide_events(event_type);
CREATE INDEX idx_ide_events_file_path ON ide_events(file_path);
CREATE INDEX idx_ide_events_data ON ide_events USING gin (event_data);

CREATE TRIGGER update_ide_event_updated_at
    BEFORE UPDATE ON ide_events
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Language statistics table
CREATE TABLE language_statistics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    coding_session_id UUID NOT NULL,
    language VARCHAR(100) NOT NULL,
    lines_of_code INTEGER NOT NULL DEFAULT 0,
    characters_written INTEGER NOT NULL DEFAULT 0,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_coding_session
        FOREIGN KEY (coding_session_id)
        REFERENCES coding_sessions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_language_statistics_session ON language_statistics(coding_session_id);
CREATE INDEX idx_language_statistics_language ON language_statistics(language);
CREATE INDEX idx_language_statistics_timestamp ON language_statistics(timestamp);

CREATE TRIGGER update_language_statistics_updated_at
    BEFORE UPDATE ON language_statistics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Activity summaries table (for caching aggregated statistics)
CREATE TABLE activity_summaries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    date DATE NOT NULL,
    total_coding_time INTEGER NOT NULL DEFAULT 0,
    language_breakdown JSONB,
    git_activity_count INTEGER NOT NULL DEFAULT 0,
    lines_changed INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,
    CONSTRAINT unique_daily_summary UNIQUE (project_id, date)
);

CREATE INDEX idx_activity_summaries_project_date ON activity_summaries(project_id, date);

CREATE TRIGGER update_activity_summary_updated_at
    BEFORE UPDATE ON activity_summaries
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create views for common queries
CREATE VIEW active_sessions AS
SELECT 
    cs.*,
    p.name as project_name,
    p.path as project_path
FROM coding_sessions cs
JOIN projects p ON cs.project_id = p.id
WHERE cs.end_time IS NULL;

CREATE VIEW daily_activity AS
SELECT 
    p.id as project_id,
    p.name as project_name,
    DATE(cs.start_time) as date,
    COUNT(DISTINCT cs.id) as session_count,
    SUM(EXTRACT(EPOCH FROM (COALESCE(cs.end_time, CURRENT_TIMESTAMP) - cs.start_time))/3600) as coding_hours,
    COUNT(DISTINCT ga.id) as git_commits
FROM projects p
LEFT JOIN coding_sessions cs ON p.id = cs.project_id
LEFT JOIN git_activities ga ON cs.id = ga.coding_session_id AND ga.activity_type = 'COMMIT'
GROUP BY p.id, p.name, DATE(cs.start_time);