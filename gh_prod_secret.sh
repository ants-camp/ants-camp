grep -v '^#' .env | grep -v '^$' | while IFS='=' read -r key value
do
  key="$(echo "$key" | xargs)"
  value="$(echo "$value" | sed 's/\r$//')"
  echo "setting secret: $key"
  gh secret set "$key" \
    --repo ant-camp/ant-camp-config \
    --env antcamp-prod \
    --body "$value"
done
