# Security Setup Guide

## Protecting Sensitive Credentials

This project uses `application.properties` to store sensitive credentials like API keys and database passwords. To keep these secure:

### Initial Setup

1. **Copy the template file:**
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```

2. **Edit `application.properties` with your actual credentials:**
   - Database username and password
   - IBM Watsonx API Key
   - IBM Watsonx Project ID

3. **Never commit `application.properties` to Git**
   - The file is already in `.gitignore`
   - Your local file will remain on your machine only

### What's Protected

The following sensitive information is protected:
- ✅ Database credentials (`quarkus.datasource.username`, `quarkus.datasource.password`)
- ✅ IBM Watsonx API Key (`ibm.watsonx.api-key`)
- ✅ IBM Watsonx Project ID (`ibm.watsonx.project-id`)

### Files in Version Control

- ✅ `application.properties.example` - Template with placeholder values (safe to commit)
- ❌ `application.properties` - Your actual credentials (ignored by Git)

### Verification

To verify your setup is secure:
```bash
git status
```

You should NOT see `application.properties` in the list of files to be committed.

### Team Collaboration

When sharing this project:
1. Share the `application.properties.example` file
2. Team members create their own `application.properties` from the template
3. Each developer uses their own credentials

## Important Notes

⚠️ **Never commit API keys or passwords to version control**
⚠️ **Rotate your API keys if they were accidentally exposed**
⚠️ **Use environment variables for production deployments**