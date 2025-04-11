# Link Shortener Evaluation

This project contains evaluation scripts for the Link Shortener application. Follow the steps below to set up and run the evaluations.

## Prerequisites

1. **Install [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/):**  
   k6 is required to run the frontend evaluation scripts. Follow the installation guide for your operating system.

2. **Install Node.js dependencies:**  
   Ensure you have Node.js installed, then run the following command to install the required dependencies:
   ```bash
   npm install
   ```

## Running Evaluations

The evaluation scripts are defined in the `scripts` section of `package.json`. Use the following commands to run the evaluations:

### Backend Evaluation

```bash
npm run be
```

### Frontend Evaluations

- **GET Requests:**
  ```bash
  npm run fe_get
  ```
- **CREATE Requests:**
  ```bash
  npm run fe_create
  ```
- **Combined GET and CREATE Requests:**
  ```bash
  npm run fe_both
  ```

### Full Evaluation (Backend + Frontend)

- **GET Requests:**
  ```bash
  npm run eval_get
  ```
- **CREATE Requests:**
  ```bash
  npm run eval_create
  ```
- **Combined GET and CREATE Requests:**
  ```bash
  npm run eval_both
  ```

## Notes

- Ensure the backend server is running before executing any evaluation scripts.
- Refer to the individual script files in the `src` directory for more details on the evaluation logic.
- Change `APP_CONTAINER_NAME` and `DB_CONTAINER_NAME` in `be_eval.js` to suit your containers'name
- Change `sampleValidIds` to suit your data in MySQL
- Change other parameters in eval files to suit your test
