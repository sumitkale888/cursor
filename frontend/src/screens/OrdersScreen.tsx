import React, {useCallback, useEffect, useState} from 'react';
import {
  FlatList,
  RefreshControl,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {parseApiError} from '../api/client';
import * as orderApi from '../api/orderApi';
import {ErrorView} from '../components/ErrorView';
import {Header} from '../components/Header';
import {LoadingSpinner} from '../components/LoadingSpinner';
import {OrdersStackParamList} from '../navigation/types';
import {OrderSummary} from '../types';
import {colors} from '../theme/colors';
import {radius, spacing} from '../theme/spacing';
import {formatCurrency, formatDate} from '../utils/format';

type Props = NativeStackScreenProps<OrdersStackParamList, 'Orders'>;

const STATUS_COLORS: Record<string, string> = {
  PENDING: colors.accent,
  CONFIRMED: colors.primary,
  DELIVERED: colors.success,
  CANCELLED: colors.error,
};

export function OrdersScreen({navigation}: Props) {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadOrders = useCallback(async () => {
    setError(null);
    try {
      const data = await orderApi.getOrderHistory();
      setOrders(data);
    } catch (err) {
      setError(parseApiError(err).message);
    }
  }, []);

  useEffect(() => {
    async function init() {
      setLoading(true);
      await loadOrders();
      setLoading(false);
    }
    init();
  }, [loadOrders]);

  const onRefresh = async () => {
    setRefreshing(true);
    await loadOrders();
    setRefreshing(false);
  };

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorView message={error} onRetry={loadOrders} />;
  }

  const renderItem = ({item}: {item: OrderSummary}) => (
    <TouchableOpacity
      style={styles.card}
      onPress={() =>
        navigation.navigate('OrderDetail', {orderId: item.id})
      }
      activeOpacity={0.85}>
      <View style={styles.cardHeader}>
        <Text style={styles.orderId}>Order #{item.id}</Text>
        <View
          style={[
            styles.badge,
            {backgroundColor: STATUS_COLORS[item.status] ?? colors.textLight},
          ]}>
          <Text style={styles.badgeText}>{item.status}</Text>
        </View>
      </View>
      <Text style={styles.date}>{formatDate(item.createdAt)}</Text>
      <View style={styles.cardFooter}>
        <Text style={styles.items}>{item.itemCount} items</Text>
        <Text style={styles.total}>{formatCurrency(item.totalAmount)}</Text>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <Header title="My Orders" subtitle="Order history" />
      <FlatList
        data={orders}
        keyExtractor={item => String(item.id)}
        renderItem={renderItem}
        contentContainerStyle={styles.list}
        ListEmptyComponent={
          <View style={styles.emptyWrap}>
            <Text style={styles.emptyIcon}>📦</Text>
            <Text style={styles.emptyTitle}>No orders yet</Text>
            <Text style={styles.emptyText}>
              Your order history will appear here
            </Text>
          </View>
        }
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            colors={[colors.primary]}
          />
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  list: {
    padding: spacing.lg,
    paddingBottom: spacing.xxl,
    flexGrow: 1,
  },
  card: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    padding: spacing.lg,
    marginBottom: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.xs,
  },
  orderId: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.text,
  },
  badge: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: radius.sm,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '700',
    color: colors.surface,
  },
  date: {
    fontSize: 13,
    color: colors.textLight,
    marginBottom: spacing.md,
  },
  cardFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  items: {
    fontSize: 14,
    color: colors.textSecondary,
  },
  total: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.primary,
  },
  emptyWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingTop: spacing.xxxl * 2,
  },
  emptyIcon: {
    fontSize: 56,
    marginBottom: spacing.lg,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.sm,
  },
  emptyText: {
    fontSize: 14,
    color: colors.textSecondary,
  },
});
