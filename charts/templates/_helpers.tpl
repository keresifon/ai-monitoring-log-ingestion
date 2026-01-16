{{- define "log-ingestion.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "log-ingestion.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "log-ingestion.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "log-ingestion.labels" -}}
helm.sh/chart: {{ include "log-ingestion.chart" . }}
{{ include "log-ingestion.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: ai-monitoring
{{- end }}

{{- define "log-ingestion.selectorLabels" -}}
app.kubernetes.io/name: {{ include "log-ingestion.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "log-ingestion.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "log-ingestion.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}
